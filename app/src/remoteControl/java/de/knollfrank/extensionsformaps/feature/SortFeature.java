package de.knollfrank.extensionsformaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.VisibleForTesting;

import java.util.Optional;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester;
import de.knollfrank.extensionsformaps.accessibility.StopCountDetector;

// FK-TODO: refactor
public class SortFeature implements AccessibilityFeature, StopCountDetector.StopCountListener {

    private static final String TAG = SortFeature.class.getSimpleName();

    private final AccessibilityService accessibilityService;
    private final WindowManager windowManager;
    private final RouteUrlRequester routeUrlRequester;
    private final RouteUrlRequester.RouteUrlCallback onRouteUrlExtracted;

    private Optional<View> buttons = Optional.empty();
    // FK-TODO: make Optional<Rect>
    private final Rect lastStopCountBounds = new Rect();

    public SortFeature(final AccessibilityService accessibilityService,
                       final RouteUrlRequester routeUrlRequester,
                       final RouteUrlRequester.RouteUrlCallback onRouteUrlExtracted) {
        this.accessibilityService = accessibilityService;
        this.windowManager = (WindowManager) accessibilityService.getSystemService(Context.WINDOW_SERVICE);
        this.routeUrlRequester = routeUrlRequester;
        this.onRouteUrlExtracted = onRouteUrlExtracted;
    }

    @Override
    public void onStopCountUpdated(final int stopCount, final Rect stopCountBounds) {
        this.lastStopCountBounds.set(stopCountBounds);
        updateButtonPositions();
    }

    @Override
    public void onStopCountLost() {
        this.lastStopCountBounds.setEmpty();
        removeButtons();
    }

    @Override
    public void reset() {
        Log.d(TAG, "Full reset of SortFeature.");
        this.lastStopCountBounds.setEmpty();
        removeButtons();
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
    }

    @Override
    public void onDestroy() {
        removeButtons();
    }

    private void updateButtonPositions() {
        if (lastStopCountBounds.isEmpty()) {
            removeButtons();
            return;
        }
        buttons.ifPresentOrElse(
                buttons -> {
                    final WindowManager.LayoutParams params = (WindowManager.LayoutParams) buttons.getLayoutParams();
                    updateLayoutParams(params, lastStopCountBounds);
                    windowManager.updateViewLayout(buttons, params);
                },
                () -> {
                    final View buttons = createButtons();
                    windowManager.addView(buttons, getButtonsLayoutParams(lastStopCountBounds));
                    this.buttons = Optional.of(buttons);
                });
    }

    private View createButtons() {
        final LinearLayout buttons = new LinearLayout(accessibilityService);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER_VERTICAL);
        buttons.addView(createSortButton());
        buttons.addView(createSettingsButton());
        return buttons;
    }

    private View createSortButton() {
        final Button sortButton = new Button(accessibilityService);
        sortButton.setText(accessibilityService.getString(R.string.sort_button_label));
        sortButton.setTextColor(Color.parseColor("#8AB4F8"));
        sortButton.setAllCaps(false);
        sortButton.setGravity(Gravity.CENTER);
        sortButton.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
        sortButton.setMinimumHeight(0);
        sortButton.setMinimumWidth(0);
        sortButton.setBackground(getButtonShape());
        sortButton.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        sortButton.setOnClickListener(view -> routeUrlRequester.requestRouteUrl(onRouteUrlExtracted));
        return sortButton;
    }

    private View createSettingsButton() {
        final Button settingsButton = new Button(accessibilityService);
        settingsButton.setText(accessibilityService.getString(R.string.settings_configure_button_text));
        settingsButton.setTextColor(Color.WHITE);
        settingsButton.setGravity(Gravity.CENTER);
        settingsButton.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        settingsButton.setMinimumHeight(0);
        settingsButton.setMinimumWidth(0);
        settingsButton.setMinWidth(0);
        settingsButton.setMinHeight(0);
        settingsButton.setBackground(getButtonShape());
        // FK-TODO: der Settingsbutton sollte hier nur so dargestellt werden, wie er selbst aussehen soll. Dass ein Zwischenraum zum Sortbutton existieren soll, MUISS im LinearLayout in createButtons() implementiert werden.
        final LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dpToPx(34),
                        LinearLayout.LayoutParams.MATCH_PARENT);
        params.leftMargin = dpToPx(4);
        settingsButton.setLayoutParams(params);
        settingsButton.setOnClickListener(view -> new SettingsDialog(accessibilityService).show());
        return settingsButton;
    }

    private GradientDrawable getButtonShape() {
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(17));
        shape.setColor(Color.parseColor("#3C4043"));
        shape.setStroke(dpToPx(2), Color.parseColor("#D4AF37"));
        return shape;
    }

    private WindowManager.LayoutParams getButtonsLayoutParams(final Rect targetRect) {
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
        return dpToPx(dp, accessibilityService.getResources().getDisplayMetrics());
    }

    public static int dpToPx(final int dp, final DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    private void removeButtons() {
        buttons.ifPresent(
                buttons -> {
                    windowManager.removeView(buttons);
                    this.buttons = Optional.empty();
                });
    }

    @VisibleForTesting
    Optional<View> getButtons() {
        return buttons;
    }
}
