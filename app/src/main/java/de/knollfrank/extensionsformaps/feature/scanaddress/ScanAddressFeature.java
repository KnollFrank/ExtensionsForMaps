package de.knollfrank.extensionsformaps.feature.scanaddress;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_APP_PACKAGE;
import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_MAPS_PACKAGE;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.Optional;
import java.util.stream.Collectors;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.common.DisplayUtils;
import de.knollfrank.extensionsformaps.common.Optionals;
import de.knollfrank.extensionsformaps.feature.AccessibilityFeature;

// FK-TODO: refactor
public class ScanAddressFeature implements AccessibilityFeature {

    private static final String TAG = ScanAddressFeature.class.getSimpleName();
    private static final ResourceName SEARCH_EDIT_TEXT_ID = ResourceNameFactory.createGoogleMapsResourceName("search_omnibox_edit_text");
    private static final ResourceName AI_MODE_CHIP_ID = ResourceNameFactory.createGoogleAppResourceName("googleapp_sbn_aim_chip");
    private static final ResourceName AIM_CAMERA_ID = ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_camera");
    private static final ResourceName AIM_INPUT_TEXT_ID = ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_autocomplete_text_input");
    private static final ResourceName AIM_SEND_BUTTON_ID = ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_enter_button");

    private enum State {
        IDLE,
        AWAITING_AI_MODE_CLICK,
        AI_MODE_CLICKED,
        AWAITING_CAMERA_BUTTON_CLICK,
        CAMERA_BUTTON_CLICKED,
        FILLING_PROMPT,
        PROMPT_FILLED,
        SENDING_PROMPT,
        AWAITING_RESPONSE
    }

    private final AccessibilityService accessibilityService;
    private final WindowManager windowManager;
    private final GoogleAppContext googleAppContext;
    private Optional<View> scanButtonOverlay = Optional.empty();
    private final Rect lastEditTextFieldBounds = new Rect();
    private Optional<String> address = Optional.empty();
    private State state = State.IDLE;
    private long lastActionTime = 0;
    private int clickRetries = 0;

    public ScanAddressFeature(final AccessibilityService accessibilityService,
                              final GoogleAppContext googleAppContext) {
        this.accessibilityService = accessibilityService;
        this.windowManager = (WindowManager) accessibilityService.getSystemService(Context.WINDOW_SERVICE);
        this.googleAppContext = googleAppContext;
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        address.ifPresent(
                address -> {
                    final boolean success = pasteAddress(root, address);
                    if (success) {
                        this.address = Optional.empty();
                        state = State.IDLE;
                    }
                });
        updateScanButton(root);
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (isNotGoogleApp(root)) {
            return;
        }
        Log.d(TAG, "onGoogleAppEvent called in state: " + state);
        if (address.isPresent()) {
            if (System.currentTimeMillis() - lastActionTime > 1000) {
                returnToMaps();
                lastActionTime = System.currentTimeMillis();
            }
            return;
        }
        if (tryExtractAIResponse(root)) {
            return;
        }
        if (state == State.AWAITING_AI_MODE_CLICK) {
            clickAIModeButtonIfFound(root);
            return;
        }
        if (state == State.AI_MODE_CLICKED || state == State.AWAITING_CAMERA_BUTTON_CLICK) {
            clickCameraButtonIfFound(root);
            return;
        }
        if (state == State.CAMERA_BUTTON_CLICKED || state == State.FILLING_PROMPT || state == State.PROMPT_FILLED || state == State.SENDING_PROMPT) {
            automateGoogleAppPromptAndSend(root);
        }
    }

    @Override
    public void onDestroy() {
        removeScanButton();
    }

    @Override
    public void reset() {
        removeScanButton();
    }

    private static boolean isNotGoogleApp(final AccessibilityNodeInfo root) {
        final Optional<String> packageName = new AccessibilityNodeInfoWrapper(root).getPackageName();
        if (packageName.isEmpty()) {
            return false;
        }
        return !GOOGLE_APP_PACKAGE.equals(packageName.get());
    }

    private boolean tryExtractAIResponse(final AccessibilityNodeInfo root) {
        final Optional<String> address = getAddress(root);
        address.ifPresent(
                _address -> {
                    this.address = Optional.of(_address);
                    Log.i(TAG, "ERGEBNIS GEFUNDEN: " + _address);
                    lastActionTime = System.currentTimeMillis();
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                });
        return address.isPresent();
    }

    private Optional<String> getAddress(final AccessibilityNodeInfo root) {
        return AIPrompt.extractAddressFromAIResponse(collectVisibleResponseText(root));
    }

    private String collectVisibleResponseText(final AccessibilityNodeInfo root) {
        return new AccessibilityNodeInfoWrapper(root)
                .streamPreOrder()
                .filter(node -> !classNameContainsEditText(node))
                .map(ScanAddressFeature::getConcatenatedTextAndContentDescription)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private static String getConcatenatedTextAndContentDescription(final AccessibilityNodeInfo node) {
        final AccessibilityNodeInfoWrapper nodeWrapper = new AccessibilityNodeInfoWrapper(node);
        return Optionals
                .streamOfPresentElements(
                        nodeWrapper::getText,
                        nodeWrapper::getContentDescription)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private void clickAIModeButtonIfFound(final AccessibilityNodeInfo root) {
        findAIModeButton(root).ifPresent(aiModeButton -> {
            Log.d(TAG, "Found AI Mode button candidate: " + aiModeButton);
            final AccessibilityNodeInfo clickableNode = findClickableAncestor(aiModeButton).orElse(aiModeButton);

            boolean clicked = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (!clicked) {
                clicked = new AccessibilityServiceWrapper(accessibilityService).click(clickableNode);
            }
            if (!clicked) {
                clicked = new AccessibilityServiceWrapper(accessibilityService).click(aiModeButton);
            }

            if (clicked) {
                Log.i(TAG, "AI Mode Button clicked successfully!");
                state = State.AWAITING_CAMERA_BUTTON_CLICK;
                lastActionTime = System.currentTimeMillis();
            } else {
                Log.w(TAG, "Failed to click AI Mode button candidate");
            }
        });
    }

    private void clickCameraButtonIfFound(final AccessibilityNodeInfo root) {
        findCameraButton(root).ifPresent(cameraButton -> {
            Log.d(TAG, "Found Camera button candidate: " + cameraButton);
            final AccessibilityNodeInfo clickableNode = findClickableAncestor(cameraButton).orElse(cameraButton);

            boolean clicked = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (!clicked) {
                clicked = new AccessibilityServiceWrapper(accessibilityService).click(clickableNode);
            }
            if (!clicked) {
                clicked = new AccessibilityServiceWrapper(accessibilityService).click(cameraButton);
            }

            if (clicked) {
                Log.i(TAG, "Camera Button ('Take a photo') clicked successfully!");
                state = State.CAMERA_BUTTON_CLICKED;
                lastActionTime = System.currentTimeMillis();
            } else {
                Log.w(TAG, "Failed to click Camera button candidate");
            }
        });
    }

    private void automateGoogleAppPromptAndSend(final AccessibilityNodeInfo root) {
        if (state == State.CAMERA_BUTTON_CLICKED || state == State.FILLING_PROMPT) {
            findInputField(root).ifPresent(inputField -> {
                Log.d(TAG, "Input field found, setting AIPrompt text...");
                if (setInputText(inputField, AIPrompt.getAIPrompt())) {
                    state = State.PROMPT_FILLED;
                    lastActionTime = System.currentTimeMillis();
                    clickRetries = 0;
                    Log.i(TAG, "AIPrompt text set successfully!");
                }
            });
        }

        if (state == State.PROMPT_FILLED || state == State.SENDING_PROMPT) {
            if (System.currentTimeMillis() - lastActionTime < 300) {
                return;
            }
            findSendButton(root).ifPresent(this::clickSendButton);
        }
    }

    private void clickSendButton(final AccessibilityNodeInfo sendButton) {
        if (clickRetries < 5) {
            Log.d(TAG, "Clicking Send button (retry " + clickRetries + ")...");
            final AccessibilityNodeInfo clickableNode = findClickableAncestor(sendButton).orElse(sendButton);
            boolean clicked = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (!clicked) {
                clicked = new AccessibilityServiceWrapper(accessibilityService).click(clickableNode);
            }
            if (!clicked) {
                clicked = new AccessibilityServiceWrapper(accessibilityService).click(sendButton);
            }

            if (clicked) {
                Log.i(TAG, "Send Button clicked successfully!");
                state = State.SENDING_PROMPT;
                lastActionTime = System.currentTimeMillis();
                clickRetries++;
            }
        } else {
            state = State.AWAITING_RESPONSE;
        }
    }

    private Optional<AccessibilityNodeInfo> findInputField(final AccessibilityNodeInfo root) {
        final AccessibilityNodeInfoWrapper wrapper = new AccessibilityNodeInfoWrapper(root);
        final Optional<AccessibilityNodeInfo> byId = wrapper.findFirstAccessibilityNodeInfoByViewId(AIM_INPUT_TEXT_ID);
        if (byId.isPresent()) {
            return byId;
        }
        return Optionals
                .streamOfPresentElements(
                        () -> findNodeByHintOrText(root, googleAppContext.askAnythingText()),
                        () -> findEditText(root))
                .findFirst();
    }

    private static Optional<AccessibilityNodeInfo> findNodeByHintOrText(final AccessibilityNodeInfo root, final String needle) {
        return new AccessibilityNodeInfoWrapper(root)
                .streamPreOrder()
                .filter(node -> {
                    final AccessibilityNodeInfoWrapper wrapper = new AccessibilityNodeInfoWrapper(node);
                    return wrapper.getHintText().map(h -> h.contains(needle)).orElse(false)
                            || wrapper.getText().map(t -> t.contains(needle)).orElse(false);
                })
                .findFirst();
    }

    private static Optional<AccessibilityNodeInfo> findEditText(final AccessibilityNodeInfo node) {
        return new AccessibilityNodeInfoWrapper(node)
                .streamPreOrder()
                .filter(ScanAddressFeature::classNameContainsEditText)
                .findFirst();
    }

    private static boolean classNameContainsEditText(final AccessibilityNodeInfo node) {
        return new AccessibilityNodeInfoWrapper(node)
                .getClassName()
                .map(className -> className.contains("EditText"))
                .orElse(false);
    }

    private Optional<AccessibilityNodeInfo> findSendButton(final AccessibilityNodeInfo root) {
        final AccessibilityNodeInfoWrapper wrapper = new AccessibilityNodeInfoWrapper(root);

        // 1. Primäre Suche nach Resource-ID
        final Optional<AccessibilityNodeInfo> byId = wrapper.findFirstAccessibilityNodeInfoByViewId(AIM_SEND_BUTTON_ID);
        if (byId.isPresent() && byId.get().isEnabled()) {
            return byId;
        }

        // 2. Sekundäre Suche nach Content-Description / Text der Google App ("Send")
        return wrapper
                .streamPreOrder()
                .filter(node -> {
                    final AccessibilityNodeInfoWrapper nodeWrapper = new AccessibilityNodeInfoWrapper(node);
                    final String contentDesc = nodeWrapper.getContentDescription().orElse("");
                    final String text = nodeWrapper.getText().orElse("");
                    final String viewId = node.getViewIdResourceName();
                    final boolean matches = (viewId != null && viewId.contains("aim_enter_button"))
                            || contentDesc.equalsIgnoreCase(googleAppContext.sendText())
                            || text.equalsIgnoreCase(googleAppContext.sendText());
                    return matches && node.isEnabled();
                })
                .findFirst();
    }

    private static Optional<AccessibilityNodeInfo> findClickableAncestor(final AccessibilityNodeInfo node) {
        AccessibilityNodeInfo current = node;
        while (current != null) {
            if (current.isClickable()) {
                return Optional.of(current);
            }
            current = current.getParent();
        }
        return Optional.empty();
    }

    private Optional<AccessibilityNodeInfo> findAIModeButton(final AccessibilityNodeInfo root) {
        final AccessibilityNodeInfoWrapper wrapper = new AccessibilityNodeInfoWrapper(root);

        // 1. Primäre Suche nach Resource-ID
        final Optional<AccessibilityNodeInfo> byId = wrapper.findFirstAccessibilityNodeInfoByViewId(AI_MODE_CHIP_ID);
        if (byId.isPresent()) {
            return byId;
        }

        // 2. Sekundäre Suche nach View-ID-Teilstring oder Text/Content-Description
        return wrapper
                .streamPreOrder()
                .filter(node -> {
                    final AccessibilityNodeInfoWrapper nodeWrapper = new AccessibilityNodeInfoWrapper(node);
                    final String viewId = node.getViewIdResourceName();
                    if (viewId != null && viewId.contains("aim_chip")) {
                        return true;
                    }
                    final String text = nodeWrapper.getText().orElse("");
                    final String contentDesc = nodeWrapper.getContentDescription().orElse("");
                    return text.equalsIgnoreCase(googleAppContext.aiModeText())
                            || contentDesc.equalsIgnoreCase(googleAppContext.aiModeText());
                })
                .findFirst();
    }

    private Optional<AccessibilityNodeInfo> findCameraButton(final AccessibilityNodeInfo root) {
        final AccessibilityNodeInfoWrapper wrapper = new AccessibilityNodeInfoWrapper(root);

        // 1. Primäre Suche nach Resource-ID (searchbox_aim_camera)
        final Optional<AccessibilityNodeInfo> byId = wrapper.findFirstAccessibilityNodeInfoByViewId(AIM_CAMERA_ID);
        if (byId.isPresent()) {
            return byId;
        }

        // 2. Sekundäre Suche nach View-ID-Teilstring oder Content-Description / Text
        return wrapper
                .streamPreOrder()
                .filter(node -> {
                    final AccessibilityNodeInfoWrapper nodeWrapper = new AccessibilityNodeInfoWrapper(node);
                    final String viewId = node.getViewIdResourceName();
                    if (viewId != null && viewId.contains("aim_camera")) {
                        return true;
                    }
                    final String contentDesc = nodeWrapper.getContentDescription().orElse("");
                    final String text = nodeWrapper.getText().orElse("");
                    return contentDesc.equalsIgnoreCase(googleAppContext.takePhotoText())
                            || text.equalsIgnoreCase(googleAppContext.takePhotoText());
                })
                .findFirst();
    }

    private void returnToMaps() {
        Log.d(TAG, "Hole Google Maps sanft in den Vordergrund...");
        Optional
                .ofNullable(accessibilityService.getPackageManager().getLaunchIntentForPackage(GOOGLE_MAPS_PACKAGE))
                .ifPresent(
                        intent -> {
                            intent
                                    .setFlags(removeResetFlag(intent))
                                    .addFlags(reorderToFront());
                            accessibilityService.startActivity(intent);
                        });
    }

    private static int removeResetFlag(final Intent intent) {
        return intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
    }

    private static int reorderToFront() {
        return Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
    }

    private boolean pasteAddress(final AccessibilityNodeInfo root, final String address) {
        final Optional<AccessibilityNodeInfo> node = new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId(SEARCH_EDIT_TEXT_ID);
        return node.isPresent() && performSetText(node.orElseThrow(), address);
    }

    private boolean setInputText(final AccessibilityNodeInfo node, final String text) {
        return performSetText(node, text) || performCopyPaste(node, text);
    }

    private static boolean performSetText(final AccessibilityNodeInfo node, final String text) {
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, getBundleForSettingText(text));
    }

    private boolean performCopyPaste(final AccessibilityNodeInfo node, final String text) {
        try {
            final ClipboardManager clipboardManager = (ClipboardManager) accessibilityService.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("text", text));
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                return node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }
        } catch (final Exception e) {
            Log.e(TAG, "Error performing copy/paste", e);
        }
        return false;
    }

    private static Bundle getBundleForSettingText(final String text) {
        final Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return bundle;
    }

    private void updateScanButton(final AccessibilityNodeInfo root) {
        ScanAddressFeature
                .findEditTextField(root)
                .ifPresentOrElse(
                        editTextField -> {
                            final Rect editTextFieldBounds = new AccessibilityNodeInfoWrapper(editTextField).getBoundsInScreen();
                            if (scanButtonOverlay.isEmpty()) {
                                showScanButton(editTextFieldBounds);
                            } else if (!lastEditTextFieldBounds.equals(editTextFieldBounds)) {
                                updateScanButtonPosition(editTextFieldBounds);
                            }
                        },
                        this::removeScanButton);
    }

    private static Optional<AccessibilityNodeInfo> findEditTextField(final AccessibilityNodeInfo root) {
        return new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId(SEARCH_EDIT_TEXT_ID);
    }

    private void showScanButton(final Rect editTextFieldBounds) {
        lastEditTextFieldBounds.set(editTextFieldBounds);
        final FrameLayout _scanButtonOverlay = new FrameLayout(accessibilityService);
        _scanButtonOverlay.addView(createScanButton(), new FrameLayout.LayoutParams(dipToPx(40), dipToPx(40)));
        try {
            windowManager.addView(_scanButtonOverlay, getScanButtonLayoutParams(editTextFieldBounds));
            scanButtonOverlay = Optional.of(_scanButtonOverlay);
        } catch (final Exception ignored) {
        }
    }

    private void updateScanButtonPosition(final Rect editTextFieldBounds) {
        lastEditTextFieldBounds.set(editTextFieldBounds);
        scanButtonOverlay.ifPresent(
                _scanButtonOverlay -> {
                    try {
                        windowManager.updateViewLayout(_scanButtonOverlay, getScanButtonLayoutParams(editTextFieldBounds));
                    } catch (final Exception exception) {
                        scanButtonOverlay = Optional.empty();
                    }
                });
    }

    private Button createScanButton() {
        final Button button = new Button(accessibilityService);
        button.setText("📷");
        button.setPadding(0, 0, 0, 0);
        button.setBackground(getScanButtonShape());
        button.setOnClickListener(
                view -> {
                    removeScanButton();
                    state = State.AWAITING_AI_MODE_CLICK;
                    clickRetries = 0;
                    lastActionTime = System.currentTimeMillis();
                    address = Optional.empty();
                    Log.d(TAG, "Scan button clicked -> set state to AWAITING_AI_MODE_CLICK and launching Google App");
                    try {
                        final Intent intent = accessibilityService.getPackageManager().getLaunchIntentForPackage(GOOGLE_APP_PACKAGE);
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            accessibilityService.startActivity(intent);
                        } else {
                            Log.e(TAG, "Google App launch intent is null");
                        }
                    } catch (final Exception e) {
                        Log.e(TAG, "Could not start Google App", e);
                    }
                });
        return button;
    }

    private GradientDrawable getScanButtonShape() {
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dipToPx(20)); // Half of 40dp for a circular button
        shape.setColor(Color.parseColor("#3C4043"));
        shape.setStroke(dipToPx(2), Color.parseColor("#D4AF37")); // Gold border
        return shape;
    }

    private WindowManager.LayoutParams getScanButtonLayoutParams(final Rect rect) {
        final WindowManager.LayoutParams scanButtonLayoutParams = new WindowManager.LayoutParams(dipToPx(40), dipToPx(40), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        scanButtonLayoutParams.gravity = Gravity.TOP | Gravity.START;
        scanButtonLayoutParams.x = rect.right - dipToPx(44);
        scanButtonLayoutParams.y = rect.centerY() - dipToPx(20);
        return scanButtonLayoutParams;
    }

    private void removeScanButton() {
        scanButtonOverlay.ifPresent(
                _scanButtonOverlay -> {
                    try {
                        windowManager.removeView(_scanButtonOverlay);
                    } catch (final Exception ignored) {
                    }
                    scanButtonOverlay = Optional.empty();
                });
    }

    private int dipToPx(final int dp) {
        return DisplayUtils.dipToPx(dp, accessibilityService.getResources().getDisplayMetrics());
    }
}
