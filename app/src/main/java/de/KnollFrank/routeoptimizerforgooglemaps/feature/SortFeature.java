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
import android.widget.Button;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.StopCountDetector;

public class SortFeature implements AccessibilityFeature, StopCountDetector.StopCountListener {

    private static final String TAG = "SortFeature";

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private final RouteUrlRequester urlRequester;

    private View sortButtonOverlay;
    private final Rect lastStopCountBounds = new Rect();
    private int lastKnownStopCount = 0;

    public SortFeature(final AccessibilityService service, final RouteUrlRequester urlRequester) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        this.urlRequester = urlRequester;
    }

    @Override
    public void onStopCountUpdated(final int count, final Rect bounds) {
        this.lastKnownStopCount = count;
        this.lastStopCountBounds.set(bounds);
        updateSortButtonPosition();
    }

    @Override
    public void onStopCountLost() {
        this.lastStopCountBounds.setEmpty();
        removeSortButton();
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
            updateSortButtonParams(lastStopCountBounds, params);
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

        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(17));
        shape.setColor(Color.parseColor("#3C4043"));
        shape.setStroke(dpToPx(2), Color.parseColor("#D4AF37"));
        button.setBackground(shape);

        button.setOnClickListener(v -> {
            Log.d(TAG, "Sort button clicked for " + lastKnownStopCount + " stops");
            urlRequester.requestUrl(url -> Log.d(TAG, "Extracted URL for SORT: " + url));
        });

        return button;
    }

    private WindowManager.LayoutParams getSortButtonLayoutParams(final Rect targetRect) {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                dpToPx(34),
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        updateSortButtonParams(targetRect, params);
        return params;
    }

    private void updateSortButtonParams(final Rect targetRect, final WindowManager.LayoutParams params) {
        params.x = targetRect.right + dpToPx(8);
        params.y = targetRect.centerY() - (dpToPx(34) / 2);
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
}
