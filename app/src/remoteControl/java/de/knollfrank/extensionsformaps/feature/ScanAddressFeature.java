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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityEventWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;

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
    private View scanButtonOverlay;
    private final Rect lastInputBounds = new Rect();
    // FK-TODO: make pendingAddress Optional
    private String pendingAddress = null;
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
                if (state != State.IDLE && pendingAddress == null) {
                    state = State.IDLE;
                }
            }
        }
        // Hier greift die originale Einsetz-Logik
        if (pendingAddress != null) {
            pasteAddress(root);
        }
        updateScanButton(root);
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (isNotGoogleAppAndNotGeminiApp(root)) {
            return;
        }

        if (pendingAddress != null) {
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
        AccessibilityNodeInfo inputField = findNodeByHint(root, "Frag Gemini");
        if (inputField == null) {
            inputField = findEditText(root);
        }

        if (inputField != null) {
            String currentText = String.valueOf(inputField.getText());

            if (state == State.IDLE && !currentText.contains("Analysiere")) {
                if (setInputText(inputField, AI_PROMPT)) {
                    state = State.FILLING_PROMPT;
                    lastActionTime = System.currentTimeMillis();
                }
                return;
            }

            if (currentText.contains("Analysiere")) {
                if (state == State.FILLING_PROMPT || state == State.IDLE) {
                    state = State.PROMPT_FILLED;
                    lastActionTime = System.currentTimeMillis();
                    clickRetries = 0;
                }

                if (state == State.PROMPT_FILLED || state == State.SENDING_PROMPT) {
                    if (System.currentTimeMillis() - lastActionTime < 1000) return;

                    final AccessibilityNodeInfo sendButton = findSendButton(root);
                    if (sendButton != null && sendButton.isEnabled()) {
                        if (clickRetries < 5) {
                            click(sendButton);
                            state = State.SENDING_PROMPT;
                            lastActionTime = System.currentTimeMillis();
                            clickRetries++;
                        } else {
                            state = State.AWAITING_RESPONSE;
                        }
                    }
                }
            }
        }
    }

    private AccessibilityNodeInfo findSendButton(AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByText("Senden");
        if (list.isEmpty()) {
            list = new AccessibilityNodeInfoWrapper(root).findAccessibilityNodeInfosByViewId(GEMINI_SEND_ID);
        }
        for (AccessibilityNodeInfo n : list) {
            if (n.isVisibleToUser()) return n;
        }
        return null;
    }

    private AccessibilityNodeInfo findEditText(final AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.getClassName() != null && node.getClassName().toString().contains("EditText"))
            return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo res = findEditText(node.getChild(i));
            if (res != null) return res;
        }
        return null;
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
                    pendingAddress = candidate.replaceAll("[\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
                    Log.i(TAG, "ERGEBNIS GEFUNDEN: " + pendingAddress);
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
        if (text.length() < 5) return false;
        String lower = text.toLowerCase();
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
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pendingAddress);
            if (et.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
                Log.d(TAG, "Adresse in Maps eingefügt.");
                pendingAddress = null;
            }
        }
    }

    private boolean setInputText(final AccessibilityNodeInfo node, final String text) {
        if (node == null) {
            return false;
        }
        final Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) return true;

        try {
            final ClipboardManager cb = (ClipboardManager) accessibilityService.getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("text", text));
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            return node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        } catch (final Exception e) {
            return false;
        }
    }

    // FK-TODO: refactor null -> Optional<AccessibilityNodeInfo>
    private AccessibilityNodeInfo findNodeByHint(final AccessibilityNodeInfo node, final String hint) {
        if (node == null) {
            return null;
        }
        final CharSequence h = node.getHintText();
        if (h != null && h.toString().contains(hint)) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            final AccessibilityNodeInfo res = findNodeByHint(node.getChild(i), hint);
            if (res != null) return res;
        }
        return null;
    }

    private void click(final AccessibilityNodeInfo node) {
        new AccessibilityServiceWrapper(accessibilityService).click(node);
    }

    private void updateScanButton(final AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> nodes = new AccessibilityNodeInfoWrapper(root).findAccessibilityNodeInfosByViewId(SEARCH_EDIT_TEXT_ID);
        if (nodes.isEmpty()) {
            removeScanButton();
            return;
        }
        final Rect b = new AccessibilityNodeInfoWrapper(nodes.get(0)).getBoundsInScreen();
        if (scanButtonOverlay == null) {
            showScanButton(b);
        } else if (!lastInputBounds.equals(b)) {
            updateScanButtonPosition(b);
        }
    }

    private void showScanButton(Rect b) {
        lastInputBounds.set(b);
        FrameLayout l = new FrameLayout(accessibilityService);
        Button btn = createButton();
        l.addView(btn, new FrameLayout.LayoutParams(dpToPx(40), dpToPx(40)));
        try {
            windowManager.addView(l, getLayoutParams(b));
            scanButtonOverlay = l;
        } catch (Exception ignored) {
        }
    }

    private void updateScanButtonPosition(Rect b) {
        lastInputBounds.set(b);
        if (scanButtonOverlay != null) try {
            windowManager.updateViewLayout(scanButtonOverlay, getLayoutParams(b));
        } catch (Exception e) {
            scanButtonOverlay = null;
        }
    }

    private Button createButton() {
        Button btn = new Button(accessibilityService);
        btn.setText("📷");
        btn.setPadding(0, 0, 0, 0);
        btn.setBackground(getButtonShape());
        btn.setOnClickListener(v -> {
            // WICHTIG: Button sofort entfernen, bevor die Kamera-Activity startet!
            removeScanButton();

            state = State.IDLE;
            clickRetries = 0;
            lastActionTime = 0;
            pendingAddress = null;
            try {
                Intent i = new Intent(accessibilityService, CaptureAddressActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                accessibilityService.startActivity(i);
            } catch (Exception e) {
                Log.e(TAG, "Could not start CaptureAddressActivity", e);
            }
        });
        return btn;
    }

    private GradientDrawable getButtonShape() {
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(20)); // Half of 40dp for a circular button
        shape.setColor(Color.parseColor("#3C4043"));
        shape.setStroke(dpToPx(2), Color.parseColor("#D4AF37")); // Gold border
        return shape;
    }

    private WindowManager.LayoutParams getLayoutParams(Rect b) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(dpToPx(40), dpToPx(40), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = b.right - dpToPx(44);
        p.y = b.centerY() - dpToPx(20);
        return p;
    }

    private void removeScanButton() {
        if (scanButtonOverlay != null) {
            try {
                windowManager.removeView(scanButtonOverlay);
            } catch (final Exception ignored) {
            }
            scanButtonOverlay = null;
        }
    }

    private int dpToPx(final int dp) {
        return SortFeature.getAnInt(dp, accessibilityService.getResources().getDisplayMetrics());
    }
}
