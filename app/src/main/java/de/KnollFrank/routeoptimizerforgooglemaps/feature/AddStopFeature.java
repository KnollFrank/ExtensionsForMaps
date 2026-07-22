package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import com.google.common.collect.Range;

import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.DummyStopAdder;
import de.KnollFrank.routeoptimizerforgooglemaps.R;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.MapsContext;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.StopCountDetector;

public class AddStopFeature implements AccessibilityFeature, StopCountDetector.StopCountListener {

    private static final String TAG = "AddStopFeature";

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private final MapsContext mapsContext;
    private final RouteUrlRequester urlRequester;

    private View highlightOverlay;
    private final Rect lastOverlayBounds = new Rect();
    private int lastKnownStopCount = 0;

    public AddStopFeature(final AccessibilityService service, final MapsContext mapsContext, final RouteUrlRequester urlRequester) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        this.mapsContext = mapsContext;
        this.urlRequester = urlRequester;
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            final String eventText = getEventText(event);
            if (isAddStopsText(eventText)) {
                if (enableEnhancedAddStopButton()) {
                    Log.d(TAG, "Stop limit reached. Requesting URL for dummy stop.");
                    urlRequester.requestUrl(url -> DummyStopAdder.addDummyStopToDirectionsUrlThenOpenInGoogleMaps(url, service));
                }
            }
        }
    }

    @Override
    public void onStopCountUpdated(final int count, final Rect bounds) {
        this.lastKnownStopCount = count;
        updateHighlightOverlay(service.getRootInActiveWindow());
    }

    @Override
    public void onStopCountLost() {
        removeHighlight();
    }

    @Override
    public void onDestroy() {
        removeHighlight();
    }

    private boolean enableEnhancedAddStopButton() {
        return Range.closedOpen(8, 25).contains(lastKnownStopCount);
    }

    private void updateHighlightOverlay(final AccessibilityNodeInfo root) {
        if (root == null) return;
        if (!Settings.canDrawOverlays(service) || !enableEnhancedAddStopButton()) {
            removeHighlight();
            root.recycle();
            return;
        }

        findAddStopsButton(root).ifPresentOrElse(
                addStopsButton -> {
                    final Rect bounds = new Rect();
                    addStopsButton.getBoundsInScreen(bounds);
                    if (highlightOverlay == null || !lastOverlayBounds.equals(bounds)) {
                        showHighlight(bounds);
                    }
                    addStopsButton.recycle();
                },
                this::removeHighlight);
        root.recycle();
    }

    private Optional<AccessibilityNodeInfo> findAddStopsButton(final AccessibilityNodeInfo root) {
        for (final String addStopsText : mapsContext.localizedAddStopsTexts()) {
            final java.util.List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(addStopsText);
            if (!nodes.isEmpty()) {
                return Optional.of(nodes.get(0));
            }
        }
        return Optional.empty();
    }

    private void showHighlight(final Rect bounds) {
        lastOverlayBounds.set(bounds);
        if (highlightOverlay == null) {
            highlightOverlay = new FrameLayout(service);
            highlightOverlay.setBackgroundResource(R.drawable.border_highlight);
            windowManager.addView(highlightOverlay, getLayoutParams(bounds));
        } else {
            final WindowManager.LayoutParams params = (WindowManager.LayoutParams) highlightOverlay.getLayoutParams();
            updateParams(bounds, params);
            windowManager.updateViewLayout(highlightOverlay, params);
        }
    }

    private void removeHighlight() {
        if (highlightOverlay != null) {
            windowManager.removeView(highlightOverlay);
            highlightOverlay = null;
            lastOverlayBounds.setEmpty();
        }
    }

    private WindowManager.LayoutParams getLayoutParams(final Rect bounds) {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        updateParams(bounds, params);
        return params;
    }

    private void updateParams(final Rect src, final WindowManager.LayoutParams dst) {
        dst.x = src.left;
        dst.y = src.top;
        dst.width = src.width();
        dst.height = src.height();
    }

    private String getEventText(final AccessibilityEvent event) {
        final StringBuilder sb = new StringBuilder();
        if (event.getContentDescription() != null) {
            sb.append(event.getContentDescription());
        }
        for (final CharSequence text : event.getText()) {
            if (text != null) {
                sb.append(text);
            }
        }
        return sb.toString();
    }

    private boolean isAddStopsText(final String text) {
        if (text == null) return false;
        for (final String addStopsText : mapsContext.localizedAddStopsTexts()) {
            if (text.contains(addStopsText)) return true;
        }
        return false;
    }
}
