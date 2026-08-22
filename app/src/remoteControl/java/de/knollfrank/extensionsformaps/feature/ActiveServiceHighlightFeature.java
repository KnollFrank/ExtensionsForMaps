package de.knollfrank.extensionsformaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import de.knollfrank.extensionsformaps.R;

public class ActiveServiceHighlightFeature implements AccessibilityFeature {

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private View highlightView;

    public ActiveServiceHighlightFeature(final AccessibilityService service) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (highlightView == null) {
            show();
        }
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
    }

    @Override
    public void onDestroy() {
        hide();
    }

    @Override
    public void reset() {
        hide();
    }

    public void show() {
        if (highlightView != null) {
            return;
        }

        highlightView = new FrameLayout(service);
        highlightView.setBackgroundResource(R.drawable.border_screen);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        windowManager.addView(highlightView, params);
    }

    private void hide() {
        if (highlightView != null) {
            windowManager.removeView(highlightView);
            highlightView = null;
        }
    }
}
