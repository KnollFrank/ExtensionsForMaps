package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.Optional;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.common.DisplayUtils;

class ScanButton {

    private final View.OnClickListener scanButtonClickListener;
    private final WindowManager windowManager;
    private final Context context;

    private Optional<View> scanButtonOverlay = Optional.empty();
    private final Rect lastEditTextFieldBounds = new Rect();

    public ScanButton(final OnClickListener scanButtonClickListener,
                      final WindowManager windowManager,
                      final Context context) {
        this.scanButtonClickListener = scanButtonClickListener;
        this.windowManager = windowManager;
        this.context = context;
    }

    public void updateScanButton(final AccessibilityNodeInfo root) {
        EditTextFieldFinder
                .findEditTextField(root)
                .ifPresentOrElse(
                        editTextField -> {
                            final Rect editTextFieldBounds = new AccessibilityNodeInfoWrapper(editTextField).getBoundsInScreen();
                            if (scanButtonOverlay.isEmpty()) {
                                showScanButton(editTextFieldBounds);
                            } else if (!lastEditTextFieldBounds.equals(editTextFieldBounds)) {
                                updateScanButtonPosition(editTextFieldBounds);
                            }
                        },
                        this::removeScanButton);
    }

    public void removeScanButton() {
        scanButtonOverlay.ifPresent(
                scanButtonOverlay -> {
                    try {
                        windowManager.removeView(scanButtonOverlay);
                    } catch (final Exception ignored) {
                    }
                    this.scanButtonOverlay = Optional.empty();
                });
    }

    private void showScanButton(final Rect editTextFieldBounds) {
        lastEditTextFieldBounds.set(editTextFieldBounds);
        final FrameLayout scanButtonOverlay = new FrameLayout(context);
        scanButtonOverlay.addView(
                createScanButton(),
                new FrameLayout.LayoutParams(
                        dipToPx(40),
                        dipToPx(40)));
        try {
            windowManager.addView(scanButtonOverlay, getScanButtonLayoutParams(editTextFieldBounds));
            this.scanButtonOverlay = Optional.of(scanButtonOverlay);
        } catch (final Exception ignored) {
        }
    }

    private void updateScanButtonPosition(final Rect editTextFieldBounds) {
        lastEditTextFieldBounds.set(editTextFieldBounds);
        scanButtonOverlay.ifPresent(
                scanButtonOverlay -> {
                    try {
                        windowManager.updateViewLayout(scanButtonOverlay, getScanButtonLayoutParams(editTextFieldBounds));
                    } catch (final Exception exception) {
                        this.scanButtonOverlay = Optional.empty();
                    }
                });
    }

    private Button createScanButton() {
        final Button button = new Button(context);
        button.setText(context.getString(R.string.scan_button_text));
        button.setPadding(0, 0, 0, 0);
        button.setBackground(getScanButtonShape());
        button.setOnClickListener(
                view -> {
                    removeScanButton();
                    scanButtonClickListener.onClick(view);
                });
        return button;
    }

    private GradientDrawable getScanButtonShape() {
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dipToPx(20)); // Half of 40dp for a circular button
        shape.setColor(Color.parseColor("#3C4043"));
        shape.setStroke(dipToPx(2), Color.parseColor("#D4AF37")); // Gold border
        return shape;
    }

    private WindowManager.LayoutParams getScanButtonLayoutParams(final Rect editTextFieldBounds) {
        final WindowManager.LayoutParams scanButtonLayoutParams = new WindowManager.LayoutParams(dipToPx(40), dipToPx(40), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        scanButtonLayoutParams.gravity = Gravity.TOP | Gravity.START;
        scanButtonLayoutParams.x = editTextFieldBounds.right - dipToPx(44);
        scanButtonLayoutParams.y = editTextFieldBounds.centerY() - dipToPx(20);
        return scanButtonLayoutParams;
    }

    private int dipToPx(final int dp) {
        return DisplayUtils.dipToPx(dp, context.getResources().getDisplayMetrics());
    }
}
