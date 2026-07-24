package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.util.Optional;

public class SpinnerOverlay {

    private final WindowManager windowManager;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Optional<View> overlay = Optional.empty();

    public SpinnerOverlay(final Context context) {
        this(
                context,
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
    }

    SpinnerOverlay(final Context context, final WindowManager windowManager) {
        this.context = context;
        this.windowManager = windowManager;
    }

    public void show() {
        mainHandler.post(() -> {
            if (overlay.isPresent()) {
                return;
            }
            final View spinnerView = createSpinnerView();
            windowManager.addView(spinnerView, getLayoutParams());
            overlay = Optional.of(spinnerView);
        });
    }

    public void hide() {
        mainHandler.post(() -> {
            overlay.ifPresent(_overlay -> {
                windowManager.removeView(_overlay);
                overlay = Optional.empty();
            });
        });
    }

    private View createSpinnerView() {
        return new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
    }

    private WindowManager.LayoutParams getLayoutParams() {
        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        return params;
    }
}
