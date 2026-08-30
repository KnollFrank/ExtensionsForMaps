package de.knollfrank.extensionsformaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import java.util.Optional;

import de.knollfrank.extensionsformaps.R;

public class ActiveServiceHighlightFeature implements AccessibilityFeature {

    private final AccessibilityService accessibilityService;
    private final WindowManager windowManager;
    private Optional<View> highlightView = Optional.empty();

    public ActiveServiceHighlightFeature(final AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
        this.windowManager = (WindowManager) accessibilityService.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (highlightView.isEmpty()) {
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
        if (highlightView.isPresent()) {
            return;
        }
        final View highlightView = new FrameLayout(accessibilityService);
        this.highlightView = Optional.of(highlightView);
        highlightView.setBackgroundResource(R.drawable.border_screen);
        windowManager.addView(highlightView, createLayoutParams());
    }

    private static WindowManager.LayoutParams createLayoutParams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
    }

    private void hide() {
        highlightView.ifPresent(
                _highlightView -> {
                    windowManager.removeView(_highlightView);
                    highlightView = Optional.empty();
                });
    }
}
