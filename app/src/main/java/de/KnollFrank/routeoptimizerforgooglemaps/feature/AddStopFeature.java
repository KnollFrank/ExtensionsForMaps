package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import com.google.common.collect.Range;

import java.util.Optional;
import java.util.function.Consumer;

import de.KnollFrank.routeoptimizerforgooglemaps.R;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContext;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.StopCountDetector;
import de.KnollFrank.routeoptimizerforgooglemaps.common.AccessibilityServices;

public class AddStopFeature implements AccessibilityFeature, StopCountDetector.StopCountListener {

    private static final String TAG = AddStopFeature.class.getSimpleName();

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private final GoogleMapsContext googleMapsContext;
    private final RouteUrlRequester routeUrlRequester;
    private final RouteUrlRequester.RouteUrlCallback onRouteUrlExtracted;
    private final AddStopAutomation automation;

    private Optional<View> highlightOverlay = Optional.empty();
    private final Rect lastOverlayBounds = new Rect();
    private int lastKnownStopCount = 0;

    public AddStopFeature(final AccessibilityService service,
                          final GoogleMapsContext googleMapsContext,
                          final RouteUrlRequester routeUrlRequester,
                          final RouteUrlRequester.RouteUrlCallback onRouteUrlExtracted) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        this.googleMapsContext = googleMapsContext;
        this.routeUrlRequester = routeUrlRequester;
        this.onRouteUrlExtracted = onRouteUrlExtracted;
        this.automation = new AddStopAutomation(service, googleMapsContext);
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        automation.onGoogleMapsEvent(root);
        if (isAddStopsButtonClick(event) && enableEnhancedAddStopButton()) {
            Log.d(TAG, "Stop limit reached. Requesting route URL.");
            routeUrlRequester.requestRouteUrl(onRouteUrlExtracted);
        }
    }

    @Override
    public void onStopCountUpdated(final int stopCount, final Rect stopCountBounds) {
        lastKnownStopCount = stopCount;
        automation.onStopCountUpdated(stopCountBounds);
        AccessibilityServices
                .getRootInActiveWindow(service)
                .ifPresent(this::updateHighlightOverlay);
    }

    @Override
    public void onStopCountLost() {
        removeHighlight();
    }

    public void reset() {
        lastKnownStopCount = 0;
        removeHighlight();
    }

    @Override
    public void onDestroy() {
        removeHighlight();
        automation.reset();
    }

    public void startAutomation() {
        automation.start();
    }

    private boolean enableEnhancedAddStopButton() {
        return Range.closedOpen(8, 25).contains(lastKnownStopCount);
    }

    private boolean isAddStopsButtonClick(final AccessibilityEvent event) {
        return event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED && isAddStopsButton(event);
    }

    private boolean isAddStopsButton(final AccessibilityEvent event) {
        return isAddStopsText(getEventText(event));
    }

    private void updateHighlightOverlay(final AccessibilityNodeInfo root) {
        if (!enableEnhancedAddStopButton()) {
            removeHighlight();
            return;
        }
        this
                .findAddStopsButton(root)
                .ifPresentOrElse(
                        new Consumer<>() {

                            @Override
                            public void accept(final AccessibilityNodeInfo addStopsButton) {
                                final Rect bounds = getBounds(addStopsButton);
                                if (highlightOverlay.isEmpty() || !lastOverlayBounds.equals(bounds)) {
                                    showHighlight(bounds);
                                }
                            }

                            private static Rect getBounds(final AccessibilityNodeInfo accessibilityNodeInfo) {
                                final Rect bounds = new Rect();
                                accessibilityNodeInfo.getBoundsInScreen(bounds);
                                return bounds;
                            }
                        },
                        this::removeHighlight);
    }

    private Optional<AccessibilityNodeInfo> findAddStopsButton(final AccessibilityNodeInfo root) {
        return root
                .findAccessibilityNodeInfosByText(googleMapsContext.addStopsText())
                .stream()
                .findFirst();
    }

    private void showHighlight(final Rect bounds) {
        lastOverlayBounds.set(bounds);
        highlightOverlay.ifPresentOrElse(
                highlightOverlay -> {
                    final WindowManager.LayoutParams params = (WindowManager.LayoutParams) highlightOverlay.getLayoutParams();
                    updateLayoutParams(params, bounds);
                    windowManager.updateViewLayout(highlightOverlay, params);
                },
                () -> {
                    final View highlightOverlay = createHighlightOverlay();
                    windowManager.addView(highlightOverlay, getLayoutParams(bounds));
                    this.highlightOverlay = Optional.of(highlightOverlay);
                });
    }

    private View createHighlightOverlay() {
        final View highlightOverlay = new FrameLayout(service);
        highlightOverlay.setBackgroundResource(R.drawable.border_highlight);
        return highlightOverlay;
    }

    private void removeHighlight() {
        highlightOverlay.ifPresent(
                highlightOverlay -> {
                    windowManager.removeView(highlightOverlay);
                    this.highlightOverlay = Optional.empty();
                    lastOverlayBounds.setEmpty();
                });
    }

    private WindowManager.LayoutParams getLayoutParams(final Rect bounds) {
        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        bounds.width(),
                        bounds.height(),
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        updateLayoutParams(params, bounds);
        return params;
    }

    private void updateLayoutParams(final WindowManager.LayoutParams dst, final Rect src) {
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

    // FK-TODO: refactor
    private boolean isAddStopsText(final String text) {
        if (text == null) return false;
        return text.contains(googleMapsContext.addStopsText());
    }
}
