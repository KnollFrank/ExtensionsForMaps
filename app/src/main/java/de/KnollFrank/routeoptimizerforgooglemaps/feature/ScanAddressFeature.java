package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
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

    private static final String TAG = "ScanAddressFeature";
    private static final String GOOGLE_PKG = "com.google.android.googlequicksearchbox";
    private static final String MAPS_PKG = "com.google.android.apps.maps";
    private static final String SEARCH_EDIT_TEXT_ID = "com.google.android.apps.maps:id/search_omnibox_edit_text";
    
    private static final String LENS_SUGGEST_EDIT_ID = "com.google.android.googlequicksearchbox:id/lensient_searchbox_suggest_text_edit";
    private static final String LENS_SEARCH_ENTRY_ID = "com.google.android.googlequicksearchbox:id/lensient_searchbox_searchresults_text_view";
    
    private static final String TOKEN_START = "START_ADDR";
    private static final String TOKEN_END = "END_ADDR";
    private static final String AI_PROMPT = "Extrahiere die Adresse ohne Namen exakt so: " + TOKEN_START + " [Adresse] " + TOKEN_END;

    private enum State {
        IDLE,
        OPENING_PANEL,
        FILLING_PROMPT,
        AWAITING_RESPONSE
    }

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private View scanButtonOverlay;
    private final Rect lastInputBounds = new Rect();
    private String pendingAddress = null;
    private State state = State.IDLE;
    private long lastActionTime = 0;

    public ScanAddressFeature(final AccessibilityService service) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override public void onServiceConnected() {}

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (MAPS_PKG.equals(String.valueOf(event.getPackageName()))) {
                if (pendingAddress != null) {
                    Log.d(TAG, "Maps wieder im Vordergrund. Adresse bereit zum Einfügen.");
                    state = State.IDLE;
                }
            }
        }
        if (pendingAddress != null) pasteAddress(root);
        updateScanButton(root);
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (root == null || !GOOGLE_PKG.equals(String.valueOf(root.getPackageName()))) return;

        // 1. Wenn wir eine Adresse haben: Sofortiger Rückzug aus Lens erzwingen!
        if (pendingAddress != null) {
            if (System.currentTimeMillis() - lastActionTime > 1500) {
                Log.d(TAG, "Adresse vorhanden. Erzwinge Rückkehr zu Maps...");
                returnToMaps();
            }
            return;
        }

        // 2. Extraktion versuchen (hat immer Vorrang vor neuem Prompt)
        if (tryExtractAIResponse()) return;

        // 3. Prompt-Eingabe (nur wenn noch keine Adresse gefunden wurde)
        AccessibilityNodeInfo inputField = findKiInput(root);
        if (inputField != null) {
            String currentText = String.valueOf(inputField.getText());
            if (currentText.contains("Extrahiere")) {
                if (state != State.AWAITING_RESPONSE) {
                    state = State.AWAITING_RESPONSE;
                    Log.d(TAG, "Prompt bereit. Bitte manuell abschicken.");
                }
                return;
            }

            if (setInputText(inputField, AI_PROMPT)) {
                state = State.FILLING_PROMPT;
                lastActionTime = System.currentTimeMillis();
            }
            return;
        }

        // 4. Einstiegspunkt
        if (state == State.IDLE || (state == State.OPENING_PANEL && System.currentTimeMillis() - lastActionTime > 5000)) {
            AccessibilityNodeInfo entry = findEntryPoint(root);
            if (entry != null) {
                clickNodeWithGesture(entry);
                lastActionTime = System.currentTimeMillis();
                state = State.OPENING_PANEL;
            }
        }
    }

    private boolean tryExtractAIResponse() {
        AccessibilityNodeInfo root = service.getRootInActiveWindow();
        if (root == null) return false;

        StringBuilder sb = new StringBuilder();
        collectAllVisibleText(root, sb);
        String fullText = sb.toString();

        Pattern pattern = Pattern.compile(TOKEN_START + "\\s*(.*?)\\s*" + TOKEN_END, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fullText);
        
        String rawAddr = null;
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (candidate.length() > 5 && !candidate.contains("[Adresse]")) {
                rawAddr = candidate;
            }
        }

        if (rawAddr != null) {
            // Bereinigung: Umbrüche und Mehrfach-Leerzeichen entfernen
            pendingAddress = rawAddr.replaceAll("[\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
            Log.i(TAG, "ADRESSE GEFUNDEN: " + pendingAddress);
            returnToMaps();
            return true;
        }
        return false;
    }

    private void returnToMaps() {
        state = State.IDLE;
        lastActionTime = System.currentTimeMillis();
        // Aktion 1: Back drücken
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        // Aktion 2: Maps aktiv nach vorne holen (als Fallback)
        try {
            Intent intent = service.getPackageManager().getLaunchIntentForPackage(MAPS_PKG);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                service.startActivity(intent);
            }
        } catch (Exception ignored) {}
    }

    private void collectAllVisibleText(AccessibilityNodeInfo node, StringBuilder sb) {
        if (node == null) return;
        String id = String.valueOf(node.getViewIdResourceName());
        if (id.contains("lensient_searchbox_suggest_text_edit")) return;

        CharSequence txt = node.getText();
        if (txt != null && txt.length() > 0) sb.append(txt).append(" ");
        CharSequence desc = node.getContentDescription();
        if (desc != null && desc.length() > 0) sb.append(desc).append(" ");
        
        for (int i = 0; i < node.getChildCount(); i++) {
            collectAllVisibleText(node.getChild(i), sb);
        }
    }

    private boolean setInputText(AccessibilityNodeInfo node, String text) {
        if (node == null) return false;
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) return true;
        
        try {
            ClipboardManager cb = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("prompt", text));
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            return node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        } catch (Exception e) { return false; }
    }

    private AccessibilityNodeInfo findEntryPoint(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo node = findNodeById(root, LENS_SEARCH_ENTRY_ID);
        if (node != null && !isEditText(node) && node.isVisibleToUser()) return node;
        String[] texts = {"Nach Infos zum Bild fragen", "Beliebige Suche", "Frage stellen"};
        for (String t : texts) {
            AccessibilityNodeInfo tn = findNodeByText(root, t);
            if (tn != null && !isEditText(tn) && tn.isVisibleToUser()) return tn;
        }
        return null;
    }

    private AccessibilityNodeInfo findKiInput(final AccessibilityNodeInfo root) {
        AccessibilityNodeInfo n = findNodeById(root, LENS_SUGGEST_EDIT_ID);
        if (n != null) return n;
        String[] texts = {"Nach Infos zum Bild fragen", "Beliebige Suche", "Frage stellen"};
        for (String t : texts) {
            AccessibilityNodeInfo tn = findNodeByText(root, t);
            if (tn != null && isEditText(tn)) return tn;
        }
        return null;
    }

    private boolean isEditText(AccessibilityNodeInfo node) {
        return node.getClassName() != null && node.getClassName().toString().contains("EditText");
    }

    private AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo node, String id) {
        if (node == null) return null;
        if (id.equals(String.valueOf(node.getViewIdResourceName()))) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo res = findNodeById(node.getChild(i), id);
            if (res != null) return res;
        }
        return null;
    }

    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo node, String text) {
        if (node == null) return null;
        CharSequence t = node.getText();
        if (t != null && t.toString().contains(text)) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo res = findNodeByText(node.getChild(i), text);
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
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0, 50));
        service.dispatchGesture(builder.build(), null, null);
    }

    private void pasteAddress(final AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(SEARCH_EDIT_TEXT_ID);
        if (!nodes.isEmpty()) {
            AccessibilityNodeInfo et = nodes.get(0);
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pendingAddress);
            if (et.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
                Log.d(TAG, "ERFOLG: Adresse in Maps eingefügt.");
                pendingAddress = null;
            }
        }
    }

    @Override public void onDestroy() { removeScanButton(); }

    private void updateScanButton(final AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(SEARCH_EDIT_TEXT_ID);
        if (nodes.isEmpty()) { removeScanButton(); return; }
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
        try { windowManager.addView(l, getLayoutParams(b)); scanButtonOverlay = l; } catch (Exception ignored) {}
    }

    private void updateScanButtonPosition(Rect b) {
        lastInputBounds.set(b);
        if (scanButtonOverlay != null) try { windowManager.updateViewLayout(scanButtonOverlay, getLayoutParams(b)); } catch (Exception e) { scanButtonOverlay = null; }
    }

    private Button createButton() {
        Button btn = new Button(service);
        btn.setText("📷");
        btn.setPadding(0, 0, 0, 0);
        btn.setOnClickListener(v -> {
            state = State.IDLE; lastActionTime = 0;
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("googlelens://v1/camera"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                service.startActivity(i);
            } catch (Exception e) {
                Intent i = service.getPackageManager().getLaunchIntentForPackage("com.google.ar.lens");
                if (i != null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); service.startActivity(i); }
            }
        });
        return btn;
    }

    private WindowManager.LayoutParams getLayoutParams(Rect b) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(dpToPx(40), dpToPx(40), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = b.right - dpToPx(44); p.y = b.centerY() - dpToPx(20);
        return p;
    }

    private void removeScanButton() { if (scanButtonOverlay != null) { try { windowManager.removeView(scanButtonOverlay); } catch (Exception ignored) {} scanButtonOverlay = null; } }
    private int dpToPx(int dp) { return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, service.getResources().getDisplayMetrics()); }
}
