package de.knollfrank.extensionsformaps.feature;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GEMINI_APP_PACKAGE;
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

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityEventWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.common.Optionals;

// FK-TODO: refactor
// FK-TODO: Verwende die Google-App https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox statt der Gemini-App für den Adressscanner.
public class ScanAddressFeature implements AccessibilityFeature {

    private static final String TAG = ScanAddressFeature.class.getSimpleName();
    private static final ResourceName SEARCH_EDIT_TEXT_ID = ResourceNameFactory.createGoogleMapsResourceName("search_omnibox_edit_text");
    private static final ResourceName GEMINI_SEND_ID = new ResourceName(GOOGLE_APP_PACKAGE, "assistant_robin_input_send_button_compose");

    // FK-TODO: use TOKEN_START instead of "START_ADDR" throughout this class
    private static final String TOKEN_START = "START_ADDR";
    public static final String TOKEN_END = "END_ADDR";
    public static final String AI_PROMPT = "Analysiere das Bild und extrahiere nur die Adresse (ohne Namen). Antwort-Format: " + TOKEN_START + " [gefundene Adresse hier einsetzen] " + TOKEN_END;

    private enum State {

        IDLE,
        FILLING_PROMPT,
        PROMPT_FILLED,
        SENDING_PROMPT,
        AWAITING_RESPONSE
    }

    private final AccessibilityService accessibilityService;
    private final WindowManager windowManager;
    private Optional<View> scanButtonOverlay;
    private final Rect lastEditTextFieldBounds = new Rect();
    private Optional<String> pendingAddress = Optional.empty();
    private State state = State.IDLE;
    private long lastActionTime = 0;
    private int clickRetries = 0;

    public ScanAddressFeature(final AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
        this.windowManager = (WindowManager) accessibilityService.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (isGoogleApp(event)) {
                if (state != State.IDLE && pendingAddress.isEmpty()) {
                    state = State.IDLE;
                }
            }
        }
        // Hier greift die originale Einsetz-Logik
        if (pendingAddress.isPresent()) {
            pasteAddress(root);
        }
        updateScanButton(root);
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (isNotGoogleAppAndNotGeminiApp(root)) {
            return;
        }
        if (pendingAddress.isPresent()) {
            // Wir haben die Adresse. Jetzt bringen wir Maps sanft nach vorne.
            if (System.currentTimeMillis() - lastActionTime > 1000) {
                returnToMaps();
                lastActionTime = System.currentTimeMillis();
            }
            return;
        }
        if (tryExtractAIResponse(root)) {
            return;
        }
        automateGemini(root);
    }

    @Override
    public void onDestroy() {
        removeScanButton();
    }

    @Override
    public void reset() {
        removeScanButton();
    }

    private static Boolean isGoogleApp(final AccessibilityEvent event) {
        return new AccessibilityEventWrapper(event)
                .getPackageName()
                .map(GOOGLE_MAPS_PACKAGE::equals)
                .orElse(false);
    }

    private static boolean isNotGoogleAppAndNotGeminiApp(final AccessibilityNodeInfo root) {
        return new AccessibilityNodeInfoWrapper(root)
                .getPackageName()
                .map(ScanAddressFeature::isNotGoogleAppAndNotGeminiApp)
                .orElse(true);
    }

    private static boolean isNotGoogleAppAndNotGeminiApp(final String packageName) {
        return !GOOGLE_APP_PACKAGE.equals(packageName) && !GEMINI_APP_PACKAGE.equals(packageName);
    }

    private void automateGemini(final AccessibilityNodeInfo root) {
        ScanAddressFeature
                .findInputField(root)
                .ifPresent(inputField -> automateGemini_setInputTextOrClickSendButton(root, inputField));
    }

    private void automateGemini_setInputTextOrClickSendButton(final AccessibilityNodeInfo root, final AccessibilityNodeInfo inputField) {
        final boolean textContainsAnalysiere = textOfNodeContainsNeedle(inputField, "Analysiere");
        if (state == State.IDLE && !textContainsAnalysiere) {
            if (setInputText(inputField, AI_PROMPT)) {
                state = State.FILLING_PROMPT;
                lastActionTime = System.currentTimeMillis();
            }
        } else if (textContainsAnalysiere) {
            if (state == State.FILLING_PROMPT || state == State.IDLE) {
                state = State.PROMPT_FILLED;
                lastActionTime = System.currentTimeMillis();
                clickRetries = 0;
            }
            if (state == State.PROMPT_FILLED || state == State.SENDING_PROMPT) {
                if (System.currentTimeMillis() - lastActionTime < 1000) {
                    return;
                }
                ScanAddressFeature
                        .findSendButton(root)
                        .ifPresent(this::clickSendButton);
            }
        }
    }

    private void clickSendButton(final AccessibilityNodeInfo sendButton) {
        if (clickRetries < 5) {
            new AccessibilityServiceWrapper(accessibilityService).click(sendButton);
            state = State.SENDING_PROMPT;
            lastActionTime = System.currentTimeMillis();
            clickRetries++;
        } else {
            state = State.AWAITING_RESPONSE;
        }
    }

    private static Optional<AccessibilityNodeInfo> findInputField(final AccessibilityNodeInfo root) {
        return Optionals
                .streamOfPresentElements(
                        () -> findNodeByHint(root, "Frag Gemini"),
                        () -> findEditText(root))
                .findFirst();
    }

    private static boolean textOfNodeContainsNeedle(final AccessibilityNodeInfo node, final String needle) {
        return new AccessibilityNodeInfoWrapper(node)
                .getText()
                .map(text -> text.contains(needle))
                .orElse(false);
    }

    private static Optional<AccessibilityNodeInfo> findSendButton(final AccessibilityNodeInfo root) {
        return findFirstIsVisibleToUser(findSendButtonCandidates(root));
    }

    private static List<AccessibilityNodeInfo> findSendButtonCandidates(final AccessibilityNodeInfo root) {
        // FK-TODO: all die deutschen Bezeichnungen sind über keys zu i18n.
        List<AccessibilityNodeInfo> sendButtonCandidates = root.findAccessibilityNodeInfosByText("Senden");
        return !sendButtonCandidates.isEmpty() ?
                sendButtonCandidates :
                new AccessibilityNodeInfoWrapper(root).findAccessibilityNodeInfosByViewId(GEMINI_SEND_ID);
    }

    private static Optional<AccessibilityNodeInfo> findFirstIsVisibleToUser(final List<AccessibilityNodeInfo> nodes) {
        return nodes
                .stream()
                .filter(AccessibilityNodeInfo::isVisibleToUser)
                .findFirst();
    }

    private static Optional<AccessibilityNodeInfo> findEditText(final AccessibilityNodeInfo node) {
        if (classNameContainsEditText(node)) {
            return Optional.of(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            final Optional<AccessibilityNodeInfo> result = findEditText(node.getChild(i));
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private static boolean classNameContainsEditText(final AccessibilityNodeInfo node) {
        return new AccessibilityNodeInfoWrapper(node)
                .getClassName()
                .map(className -> className.contains("EditText"))
                .orElse(false);
    }

    private boolean tryExtractAIResponse(final AccessibilityNodeInfo root) {
        StringBuilder sb = new StringBuilder();
        collectVisibleResponseText(root, sb);
        String fullText = sb.toString();

        if (fullText.contains(TOKEN_END)) {
            Pattern pattern = Pattern.compile("START_ADDR\\s*[*]*\\s*(.*?)\\s*[*]*\\s*END_ADDR",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(fullText);

            while (matcher.find()) {
                String candidate = matcher.group(1).trim();
                if (isValidAddress(candidate)) {
                    final String _pendingAddress =
                            candidate
                                    .replaceAll("[\\r\\n]+", " ")
                                    .replaceAll("\\s{2,}", " ")
                                    .trim();
                    pendingAddress = Optional.of(_pendingAddress);
                    Log.i(TAG, "ERGEBNIS GEFUNDEN: " + _pendingAddress);
                    lastActionTime = System.currentTimeMillis();
                    // Erster sanfter Versuch via BACK
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    return true;
                }
            }
        }
        return false;
    }

    private void collectVisibleResponseText(AccessibilityNodeInfo node, StringBuilder sb) {
        if (node == null) return;
        String className = String.valueOf(node.getClassName());
        if (!className.contains("EditText")) {
            CharSequence txt = node.getText();
            if (txt != null && txt.length() > 0) sb.append(txt).append(" ");
            CharSequence desc = node.getContentDescription();
            if (desc != null && desc.length() > 0) sb.append(desc).append(" ");
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectVisibleResponseText(node.getChild(i), sb);
        }
    }

    private boolean isValidAddress(String text) {
        if (text.length() < 5) {
            return false;
        }
        final String lower = text.toLowerCase();
        return !lower.contains("gefundene adresse") && !lower.contains("extrahiere") && text.matches(".*\\d+.*");
    }

    private void returnToMaps() {
        Log.d(TAG, "Hole Google Maps sanft in den Vordergrund...");
        // Wir nutzen einen Intent, der nur die existierende Instanz nach vorne holt
        Intent intent = accessibilityService.getPackageManager().getLaunchIntentForPackage(GOOGLE_MAPS_PACKAGE);
        if (intent != null) {
            // WICHTIG: Wir loeschen den Reset-Flag, falls vorhanden
            intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            // Wir fuegen REORDER_TO_FRONT hinzu, um nur die bestehende Task anzuzeigen
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            accessibilityService.startActivity(intent);
        }
    }

    private void pasteAddress(final AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> nodes = new AccessibilityNodeInfoWrapper(root).findAccessibilityNodeInfosByViewId(SEARCH_EDIT_TEXT_ID);
        if (!nodes.isEmpty()) {
            AccessibilityNodeInfo et = nodes.get(0);
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pendingAddress.orElse(null));
            if (et.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
                Log.d(TAG, "Adresse in Maps eingefügt.");
                pendingAddress = Optional.empty();
            }
        }
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
            clipboardManager.setPrimaryClip(ClipData.newPlainText("text", text));
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            return node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        } catch (final Exception e) {
            return false;
        }
    }

    private static Bundle getBundleForSettingText(final String text) {
        final Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return bundle;
    }

    // FK-TODO: dieses Muster kommt öfter vor
    private static Optional<AccessibilityNodeInfo> findNodeByHint(final AccessibilityNodeInfo node, final String hint) {
        if (nodeContainsHint(node, hint)) {
            return Optional.of(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            final Optional<AccessibilityNodeInfo> result = findNodeByHint(node.getChild(i), hint);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private static Boolean nodeContainsHint(final AccessibilityNodeInfo node, final String hint) {
        return new AccessibilityNodeInfoWrapper(node)
                .getHintText()
                .map(hintText -> hintText.contains(hint))
                .orElse(false);
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
        _scanButtonOverlay.addView(createScanButton(), new FrameLayout.LayoutParams(dpToPx(40), dpToPx(40)));
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
                    // WICHTIG: Button sofort entfernen, bevor die Kamera-Activity startet!
                    removeScanButton();
                    state = State.IDLE;
                    clickRetries = 0;
                    lastActionTime = 0;
                    pendingAddress = Optional.empty();
                    try {
                        final Intent intent = new Intent(accessibilityService, CaptureAddressActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        accessibilityService.startActivity(intent);
                    } catch (final Exception e) {
                        Log.e(TAG, "Could not start CaptureAddressActivity", e);
                    }
                });
        return button;
    }

    private GradientDrawable getScanButtonShape() {
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(20)); // Half of 40dp for a circular button
        shape.setColor(Color.parseColor("#3C4043"));
        shape.setStroke(dpToPx(2), Color.parseColor("#D4AF37")); // Gold border
        return shape;
    }

    private WindowManager.LayoutParams getScanButtonLayoutParams(final Rect rect) {
        WindowManager.LayoutParams scanButtonLayoutParams = new WindowManager.LayoutParams(dpToPx(40), dpToPx(40), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        scanButtonLayoutParams.gravity = Gravity.TOP | Gravity.START;
        scanButtonLayoutParams.x = rect.right - dpToPx(44);
        scanButtonLayoutParams.y = rect.centerY() - dpToPx(20);
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

    private int dpToPx(final int dp) {
        return SortFeature.getAnInt(dp, accessibilityService.getResources().getDisplayMetrics());
    }
}
