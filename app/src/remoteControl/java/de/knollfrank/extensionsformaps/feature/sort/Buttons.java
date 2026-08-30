package de.knollfrank.extensionsformaps.feature.sort;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.Optional;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.common.DisplayUtils;

class Buttons {

    private Optional<View> buttonContainer = Optional.empty();
    private final WindowManager windowManager;
    private final Context context;
    private final OnClickListeners onClickListeners;

    public Buttons(final WindowManager windowManager,
                   final Context context,
                   final OnClickListeners onClickListeners) {
        this.windowManager = windowManager;
        this.context = context;
        this.onClickListeners = onClickListeners;
    }

    public void createOrUpdateButtons(final Rect stopCountBounds) {
        buttonContainer.ifPresentOrElse(
                buttonContainer -> updateButtons(stopCountBounds, buttonContainer),
                () -> createButtons(stopCountBounds));
    }

    public void removeButtons() {
        buttonContainer.ifPresent(
                buttons -> {
                    windowManager.removeView(buttons);
                    this.buttonContainer = Optional.empty();
                });
    }

    public Optional<View> getButtonContainer() {
        return buttonContainer;
    }

    private void updateButtons(final Rect stopCountBounds, final View buttonContainer) {
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) buttonContainer.getLayoutParams();
        updateLayoutParams(params, stopCountBounds);
        windowManager.updateViewLayout(buttonContainer, params);
    }

    private void createButtons(final Rect stopCountBounds) {
        final View buttons = createButtons();
        windowManager.addView(buttons, getButtonsLayoutParams(stopCountBounds));
        this.buttonContainer = Optional.of(buttons);
    }

    private View createButtons() {
        final LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER_VERTICAL);
        buttons.addView(createSortButton());
        buttons.addView(createSpace(dipToPx(4)));
        buttons.addView(createSettingsButton());
        return buttons;
    }

    private View createSpace(final int widthPx) {
        final Space space = new Space(context);
        space.setLayoutParams(
                new LinearLayout.LayoutParams(
                        widthPx,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        return space;
    }

    private View createSortButton() {
        final Button sortButton = new Button(context);
        sortButton.setText(context.getString(R.string.sort_button_label));
        sortButton.setTextColor(Color.parseColor("#8AB4F8"));
        sortButton.setAllCaps(false);
        sortButton.setGravity(Gravity.CENTER);
        sortButton.setPadding(dipToPx(12), dipToPx(6), dipToPx(12), dipToPx(6));
        sortButton.setMinimumHeight(0);
        sortButton.setMinimumWidth(0);
        sortButton.setBackground(getButtonShape());
        sortButton.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        sortButton.setOnClickListener(onClickListeners.sortButtonListener());
        return sortButton;
    }

    private View createSettingsButton() {
        final Button settingsButton = new Button(context);
        settingsButton.setText(context.getString(R.string.settings_configure_button_text));
        settingsButton.setTextColor(Color.WHITE);
        settingsButton.setGravity(Gravity.CENTER);
        settingsButton.setPadding(dipToPx(4), dipToPx(4), dipToPx(4), dipToPx(4));
        settingsButton.setMinimumHeight(0);
        settingsButton.setMinimumWidth(0);
        settingsButton.setMinWidth(0);
        settingsButton.setMinHeight(0);
        settingsButton.setBackground(getButtonShape());
        settingsButton.setLayoutParams(
                new LinearLayout.LayoutParams(
                        dipToPx(34),
                        LinearLayout.LayoutParams.MATCH_PARENT));
        settingsButton.setOnClickListener(onClickListeners.settingsButtonListener());
        return settingsButton;
    }

    private GradientDrawable getButtonShape() {
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dipToPx(17));
        shape.setColor(Color.parseColor("#3C4043"));
        shape.setStroke(dipToPx(2), Color.parseColor("#D4AF37"));
        return shape;
    }

    private WindowManager.LayoutParams getButtonsLayoutParams(final Rect targetRect) {
        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        dipToPx(34),
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        updateLayoutParams(params, targetRect);
        return params;
    }

    private void updateLayoutParams(final WindowManager.LayoutParams dst, final Rect src) {
        dst.x = src.right + dipToPx(8);
        dst.y = src.centerY() - (dipToPx(34) / 2);
    }

    private int dipToPx(final int dp) {
        return DisplayUtils.dipToPx(dp, context.getResources().getDisplayMetrics());
    }
}
