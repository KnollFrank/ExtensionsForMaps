package de.knollfrank.extensionsformaps.feature.scanaddress;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_APP_PACKAGE;
import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_MAPS_PACKAGE;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.feature.AccessibilityFeature;

// FK-TODO: refactor
public class ScanAddressFeature implements AccessibilityFeature {

    private static final String TAG = ScanAddressFeature.class.getSimpleName();

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
    private final GoogleAppContext googleAppContext;
    private final ScanButton scanButton;
    private Optional<String> address = Optional.empty();
    private State state = State.IDLE;
    private long lastActionTime = 0;
    private int clickRetries = 0;

    public ScanAddressFeature(final AccessibilityService accessibilityService,
                              final GoogleAppContext googleAppContext) {
        this.accessibilityService = accessibilityService;
        this.googleAppContext = googleAppContext;
        this.scanButton =
                new ScanButton(
                        createScanButtonClickListener(accessibilityService),
                        new AccessibilityServiceWrapper(accessibilityService).getWindowManager(),
                        accessibilityService);
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
        scanButton.updateScanButton(root);
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        Log.d(TAG, "onGoogleAppEvent called in state: " + state);
        if (address.isPresent()) {
            if (System.currentTimeMillis() - lastActionTime > 1000) {
                returnToGoogleMaps();
                lastActionTime = System.currentTimeMillis();
            }
            return;
        }
        if (tryExtractAIResponse(root)) {
            return;
        }
        switch (state) {
            case AWAITING_AI_MODE_CLICK -> clickAIModeButtonIfFound(root);
            case AI_MODE_CLICKED, AWAITING_CAMERA_BUTTON_CLICK -> clickCameraButtonIfFound(root);
            case CAMERA_BUTTON_CLICKED, FILLING_PROMPT, PROMPT_FILLED, SENDING_PROMPT ->
                    automateGoogleAppPromptAndSend(root);
        }
    }

    @Override
    public void onDestroy() {
        scanButton.removeScanButton();
    }

    @Override
    public void reset() {
        scanButton.removeScanButton();
    }

    private OnClickListener createScanButtonClickListener(final AccessibilityService accessibilityService) {
        return new OnClickListener() {

            @Override
            public void onClick(final View view) {
                scanButton.removeScanButton();
                state = State.AWAITING_AI_MODE_CLICK;
                clickRetries = 0;
                lastActionTime = System.currentTimeMillis();
                address = Optional.empty();
                Log.d(ScanAddressFeature.TAG, "Scan button clicked -> set state to AWAITING_AI_MODE_CLICK and launching Google App");
                try {
                    final Intent intent = accessibilityService.getPackageManager().getLaunchIntentForPackage(GOOGLE_APP_PACKAGE);
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        accessibilityService.startActivity(intent);
                    } else {
                        Log.e(ScanAddressFeature.TAG, "Google App launch intent is null");
                    }
                } catch (final Exception e) {
                    Log.e(ScanAddressFeature.TAG, "Could not start Google App", e);
                }
            }
        };
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
        return AIPrompt.extractAddressFromAIResponse(
                new VisibleResponseTextProvider(ScanAddressFeature::classNameContainsEditText)
                        .collectVisibleResponseText(root));
    }

    private void clickAIModeButtonIfFound(final AccessibilityNodeInfo root) {
        new AIModeButtonFinder(googleAppContext)
                .findAIModeButton(root)
                .ifPresent(
                        aiModeButton -> {
                            Log.d(TAG, "Found AI Mode button candidate: " + aiModeButton);
                            final boolean clicked = clickButton(aiModeButton);
                            if (clicked) {
                                Log.i(TAG, "AI Mode Button clicked successfully!");
                                state = State.AWAITING_CAMERA_BUTTON_CLICK;
                                lastActionTime = System.currentTimeMillis();
                            } else {
                                Log.w(TAG, "Failed to click AI Mode button candidate");
                            }
                        });
    }

    private boolean clickButton(final AccessibilityNodeInfo button) {
        return new ButtonClick(accessibilityService).clickButton(button);
    }

    private void clickCameraButtonIfFound(final AccessibilityNodeInfo root) {
        new CameraButtonFinder(googleAppContext)
                .findCameraButton(root)
                .ifPresent(
                        cameraButton -> {
                            Log.d(TAG, "Found Camera button candidate: " + cameraButton);
                            final boolean clicked = clickButton(cameraButton);
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
            new InputFieldFinder(ScanAddressFeature::classNameContainsEditText, googleAppContext)
                    .findInputField(root)
                    .ifPresent(
                            inputField -> {
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
            new SendButtonFinder(googleAppContext)
                    .findSendButton(root)
                    .ifPresent(this::clickSendButton);
        }
    }

    private void clickSendButton(final AccessibilityNodeInfo sendButton) {
        if (clickRetries < 5) {
            Log.d(TAG, "Clicking Send button (retry " + clickRetries + ")...");
            final boolean clicked = clickButton(sendButton);
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

    private static boolean classNameContainsEditText(final AccessibilityNodeInfo node) {
        return new AccessibilityNodeInfoWrapper(node)
                .getClassName()
                .map(className -> className.contains("EditText"))
                .orElse(false);
    }

    private void returnToGoogleMaps() {
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
        final Optional<AccessibilityNodeInfo> node = EditTextFieldFinder.findEditTextField(root);
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
}
