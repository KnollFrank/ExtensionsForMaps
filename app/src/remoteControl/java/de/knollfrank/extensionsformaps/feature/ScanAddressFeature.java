package de.knollfrank.extensionsformaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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

public class ScanAddressFeature implements AccessibilityFeature {

    private static final String TAG = ScanAddressFeature.class.getSimpleName();
    private static final String GOOGLE_PKG = "com.google.android.googlequicksearchbox";
    private static final String GEMINI_PKG = "com.google.android.apps.bard";
    private static final String MAPS_PKG = "com.google.android.apps.maps";
    private static final String SEARCH_EDIT_TEXT_ID = "com.google.android.apps.maps:id/search_omnibox_edit_text";

    private static final String GEMINI_SEND_ID = "com.google.android.googlequicksearchbox:id/assistant_robin_input_send_button_compose";

    public static final String TOKEN_START = "START_ADDR";
    public static final String TOKEN_END = "END_ADDR";
    public static final String AI_PROMPT = "Analysiere das Bild und extrahiere nur die Adresse (ohne Namen). Antwort-Format: " + TOKEN_START + " [gefundene Adresse hier einsetzen] " + TOKEN_END;

    private enum State {
        IDLE,
        FILLING_PROMPT,
        PROMPT_FILLED,
        SENDING_PROMPT,
        AWAITING_RESPONSE
    }

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private View scanButtonOverlay;
    private final Rect lastInputBounds = new Rect();
    private String pendingAddress = null;
    private State state = State.IDLE;
    private long lastActionTime = 0;
    private int clickRetries = 0;

    public ScanAddressFeature(final AccessibilityService service) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (MAPS_PKG.equals(String.valueOf(event.getPackageName()))) {
                if (state != State.IDLE && pendingAddress == null) {
                    state = State.IDLE;
                }
            }
        }
        // Hier greift die originale Einsetz-Logik
        if (pendingAddress != null) pasteAddress(root);
        updateScanButton(root);
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (root == null) return;
        String pkg = String.valueOf(root.getPackageName());
        if (!GOOGLE_PKG.equals(pkg) && !GEMINI_PKG.equals(pkg)) return;

        if (pendingAddress != null) {
            // Wir haben die Adresse. Jetzt bringen wir Maps sanft nach vorne.
            if (System.currentTimeMillis() - lastActionTime > 1000) {
                returnToMaps();
                lastActionTime = System.currentTimeMillis();
            }
            return;
        }

        if (tryExtractAIResponse(root)) return;

        automateGemini(root);
    }

    private void automateGemini(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo inputField = findNodeByHint(root, "Frag Gemini");
        if (inputField == null) inputField = findEditText(root);

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

                    AccessibilityNodeInfo sendButton = findSendButton(root);
                    if (sendButton != null && sendButton.isEnabled()) {
                        if (clickRetries < 5) {
                            clickNodeWithGesture(sendButton);
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
            list = root.findAccessibilityNodeInfosByViewId(GEMINI_SEND_ID);
        }
        for (AccessibilityNodeInfo n : list) {
            if (n.isVisibleToUser()) return n;
        }
        return null;
    }

    private AccessibilityNodeInfo findEditText(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.getClassName() != null && node.getClassName().toString().contains("EditText"))
            return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo res = findEditText(node.getChild(i));
            if (res != null) return res;
        }
        return null;
    }

    private boolean tryExtractAIResponse(AccessibilityNodeInfo root) {
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
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
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
        Intent intent = service.getPackageManager().getLaunchIntentForPackage(MAPS_PKG);
        if (intent != null) {
            // WICHTIG: Wir loeschen den Reset-Flag, falls vorhanden
            intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            // Wir fuegen REORDER_TO_FRONT hinzu, um nur die bestehende Task anzuzeigen
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            service.startActivity(intent);
        }
    }

    private void pasteAddress(final AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(SEARCH_EDIT_TEXT_ID);
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

    private boolean setInputText(AccessibilityNodeInfo node, String text) {
        if (node == null) return false;
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) return true;

        try {
            ClipboardManager cb = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("text", text));
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            return node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        } catch (Exception e) {
            return false;
        }
    }

    private AccessibilityNodeInfo findNodeByHint(AccessibilityNodeInfo node, String hint) {
        if (node == null) return null;
        CharSequence h = node.getHintText();
        if (h != null && h.toString().contains(hint)) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo res = findNodeByHint(node.getChild(i), hint);
            if (res != null) return res;
        }
        return null;
    }

    private void clickNodeWithGesture(AccessibilityNodeInfo node) {
        Rect b = new Rect();
        node.getBoundsInScreen(b);
        Path p = new Path();
        p.moveTo(b.centerX(), b.centerY());
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0, 100));
        service.dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onDestroy() {
        removeScanButton();
    }

    private void updateScanButton(final AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(SEARCH_EDIT_TEXT_ID);
        if (nodes.isEmpty()) {
            removeScanButton();
            return;
        }
        Rect b = new Rect();
        nodes.get(0).getBoundsInScreen(b);
        if (scanButtonOverlay == null) showScanButton(b);
        else if (!lastInputBounds.equals(b)) updateScanButtonPosition(b);
    }

    private void showScanButton(Rect b) {
        lastInputBounds.set(b);
        FrameLayout l = new FrameLayout(service);
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
        Button btn = new Button(service);
        btn.setText("📷");
        btn.setPadding(0, 0, 0, 0);
        btn.setBackground(getButtonShape());
        btn.setOnClickListener(v -> {
            state = State.IDLE;
            clickRetries = 0;
            lastActionTime = 0;
            pendingAddress = null;
            try {
                Intent i = new Intent(service, CaptureAddressActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                service.startActivity(i);
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
            } catch (Exception ignored) {
            }
            scanButtonOverlay = null;
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, service.getResources().getDisplayMetrics());
    }
}
