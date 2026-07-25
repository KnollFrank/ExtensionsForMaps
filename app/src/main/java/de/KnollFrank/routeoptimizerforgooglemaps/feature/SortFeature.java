package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import androidx.annotation.VisibleForTesting;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.StopCountDetector;

// FK-TODO: refactor
public class SortFeature implements AccessibilityFeature, StopCountDetector.StopCountListener {

    private static final String TAG = SortFeature.class.getSimpleName();

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private final RouteUrlRequester routeUrlRequester;
    private final RouteUrlRequester.RouteUrlCallback onRouteUrlExtracted;

    // FK-TODO: make Optional<View>
    private View sortButtonOverlay;
    private final Rect lastStopCountBounds = new Rect();
    private int lastKnownStopCount = 0;

    public SortFeature(final AccessibilityService service,
                       final RouteUrlRequester routeUrlRequester,
                       final RouteUrlRequester.RouteUrlCallback onRouteUrlExtracted) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        this.routeUrlRequester = routeUrlRequester;
        this.onRouteUrlExtracted = onRouteUrlExtracted;
    }

    @Override
    public void onStopCountUpdated(final int stopCount, final Rect stopCountBounds) {
        this.lastKnownStopCount = stopCount;
        this.lastStopCountBounds.set(stopCountBounds);
        updateSortButtonPosition();
    }

    @Override
    public void onStopCountLost() {
        Log.d(TAG, "Stop count label not found. Hiding sort button, but keeping count: " + lastKnownStopCount);
        this.lastStopCountBounds.setEmpty();
        removeSortButton();
    }

    public void reset() {
        Log.d(TAG, "Full reset of SortFeature.");
        lastKnownStopCount = 0;
        this.lastStopCountBounds.setEmpty();
        removeSortButton();
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
    }

    @Override
    public void onDestroy() {
        removeSortButton();
    }

    private void updateSortButtonPosition() {
        if (lastStopCountBounds.isEmpty()) {
            removeSortButton();
            return;
        }
        if (sortButtonOverlay == null) {
            sortButtonOverlay = createSortButton();
            windowManager.addView(sortButtonOverlay, getSortButtonLayoutParams(lastStopCountBounds));
        } else {
            final WindowManager.LayoutParams params = (WindowManager.LayoutParams) sortButtonOverlay.getLayoutParams();
            updateLayoutParams(params, lastStopCountBounds);
            windowManager.updateViewLayout(sortButtonOverlay, params);
        }
    }

    private View createSortButton() {
        final Button button = new Button(service);
        button.setText("↕️ Sortieren");
        button.setTextColor(Color.parseColor("#8AB4F8"));
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
        button.setMinimumHeight(0);
        button.setMinimumWidth(0);
        button.setBackground(getShape());
        button.setOnClickListener(view -> {
            Log.d(TAG, "Sort button clicked for " + lastKnownStopCount + " stops");
            routeUrlRequester.requestRouteUrl(onRouteUrlExtracted);
        });
        return button;
    }

    private GradientDrawable getShape() {
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(17));
        shape.setColor(Color.parseColor("#3C4043"));
        shape.setStroke(dpToPx(2), Color.parseColor("#D4AF37"));
        return shape;
    }

    private WindowManager.LayoutParams getSortButtonLayoutParams(final Rect targetRect) {
        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        dpToPx(34),
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        updateLayoutParams(params, targetRect);
        return params;
    }

    private void updateLayoutParams(final WindowManager.LayoutParams dst, final Rect src) {
        dst.x = src.right + dpToPx(8);
        dst.y = src.centerY() - (dpToPx(34) / 2);
    }

    private int dpToPx(final int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, service.getResources().getDisplayMetrics());
    }

    private void removeSortButton() {
        if (sortButtonOverlay != null) {
            windowManager.removeView(sortButtonOverlay);
            sortButtonOverlay = null;
        }
    }

    @VisibleForTesting
    View getSortButtonOverlay() {
        return sortButtonOverlay;
    }
}
