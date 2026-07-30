package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
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

public class ScanAddressFeature implements AccessibilityFeature {

    private static final String TAG = "ScanAddressFeature";
    private static final String SEARCH_EDIT_TEXT_ID = "com.google.android.apps.maps:id/search_omnibox_edit_text";

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private View scanButtonOverlay;
    private final Rect lastInputBounds = new Rect();
    private String pendingAddress = null;

    public ScanAddressFeature(final AccessibilityService service) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (pendingAddress != null) {
            pasteAddress(root);
        }
        updateScanButton(root);
    }

    private void pasteAddress(final AccessibilityNodeInfo root) {
        final List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(SEARCH_EDIT_TEXT_ID);
        if (!nodes.isEmpty()) {
            final AccessibilityNodeInfo editText = nodes.get(0);
            final Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pendingAddress);
            if (editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                Log.d(TAG, "Address successfully pasted: " + pendingAddress);
                pendingAddress = null;
            }
            editText.recycle();
        }
    }

    @Override
    public void onDestroy() {
        removeScanButton();
    }

    private void updateScanButton(final AccessibilityNodeInfo root) {
        final List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(SEARCH_EDIT_TEXT_ID);
        if (nodes.isEmpty()) {
            removeScanButton();
            return;
        }

        final AccessibilityNodeInfo inputNode = nodes.get(0);
        final Rect bounds = new Rect();
        inputNode.getBoundsInScreen(bounds);

        if (scanButtonOverlay == null) {
            showScanButton(bounds);
        } else if (!lastInputBounds.equals(bounds)) {
            updateScanButtonPosition(bounds);
        }
        inputNode.recycle();
    }

    private void showScanButton(final Rect inputBounds) {
        lastInputBounds.set(inputBounds);
        final FrameLayout layout = new FrameLayout(service);
        final Button scanButton = createButton();
        layout.addView(scanButton, new FrameLayout.LayoutParams(dpToPx(40), dpToPx(40)));

        try {
            windowManager.addView(layout, getLayoutParams(inputBounds));
            scanButtonOverlay = layout;
        } catch (WindowManager.BadTokenException e) {
            Log.e(TAG, "Failed to add scan button overlay", e);
        }
    }

    private void updateScanButtonPosition(final Rect inputBounds) {
        lastInputBounds.set(inputBounds);
        if (scanButtonOverlay != null) {
            try {
                windowManager.updateViewLayout(scanButtonOverlay, getLayoutParams(inputBounds));
            } catch (IllegalArgumentException e) {
                // View might have been removed already
                scanButtonOverlay = null;
            }
        }
    }

    private Button createButton() {
        final Button scanButton = new Button(service);
        scanButton.setText("📷");
        scanButton.setPadding(0, 0, 0, 0);
        scanButton.setOnClickListener(v -> {
            ScanAddressActivity.setCallback(address -> {
                Log.d(TAG, "Address received from scanner: " + address);
                pendingAddress = address;
            });
            final Intent intent = new Intent(service, ScanAddressActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            service.startActivity(intent);
        });
        return scanButton;
    }

    private WindowManager.LayoutParams getLayoutParams(final Rect inputBounds) {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                dpToPx(40),
                dpToPx(40),
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = inputBounds.right - dpToPx(44);
        params.y = inputBounds.centerY() - dpToPx(20);
        return params;
    }

    private void removeScanButton() {
        if (scanButtonOverlay != null) {
            try {
                windowManager.removeView(scanButtonOverlay);
            } catch (IllegalArgumentException ignored) {
            }
            scanButtonOverlay = null;
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, service.getResources().getDisplayMetrics());
    }
}
