package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Optional;

public class ProgressOverlay {

    private final WindowManager windowManager;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Optional<View> overlay = Optional.empty();
    private TextView statusTextView;

    public ProgressOverlay(final Context context) {
        this(
                context,
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
    }

    ProgressOverlay(final Context context, final WindowManager windowManager) {
        this.context = context;
        this.windowManager = windowManager;
    }

    public void show() {
        mainHandler.post(() -> {
            if (overlay.isPresent()) {
                return;
            }
            final View progressView = createProgressView();
            windowManager.addView(progressView, getLayoutParams());
            overlay = Optional.of(progressView);
        });
    }

    public void updateStatus(final String message) {
        mainHandler.post(() -> {
            if (statusTextView != null) {
                statusTextView.setText(message);
                statusTextView.setVisibility(
                        message != null && !message.isEmpty() ?
                                View.VISIBLE :
                                View.GONE);
            }
        });
    }

    public void hide() {
        mainHandler.post(() -> {
            overlay.ifPresent(_overlay -> {
                windowManager.removeView(_overlay);
                overlay = Optional.empty();
                statusTextView = null;
            });
        });
    }

    private View createProgressView() {
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#CC000000")); // semi-transparent black
        int padding = dpToPx(20);
        layout.setPadding(padding, padding, padding, padding);

        final ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        layout.addView(progressBar);

        statusTextView = new TextView(context);
        statusTextView.setTextColor(Color.WHITE);
        statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        statusTextView.setGravity(Gravity.CENTER);
        statusTextView.setVisibility(View.GONE);
        final LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.topMargin = dpToPx(10);
        layout.addView(statusTextView, textParams);

        return layout;
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

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics());
    }
}
