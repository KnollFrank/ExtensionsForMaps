package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class AddressOverlayView extends View {

    private final Paint strokePaint = new Paint();
    private final Paint fillPaint = new Paint();
    private final Rect drawRect = new Rect();
    private Rect targetRect = null;
    private int imageWidth = 0;
    private int imageHeight = 0;

    public AddressOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        strokePaint.setColor(Color.parseColor("#4285F4")); // Google Blue
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(12f);
        strokePaint.setAntiAlias(true);

        fillPaint.setColor(Color.parseColor("#334285F4")); // 20% Alpha Blue
        fillPaint.setStyle(Paint.Style.FILL);
    }

    public void updateBounds(Rect rect, int imageWidth, int imageHeight) {
        this.targetRect = rect;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (targetRect == null || targetRect.isEmpty() || imageWidth == 0 || imageHeight == 0) {
            return;
        }

        // CameraX ImageProxy is usually in landscape (e.g., 640x480).
        // On a portrait screen, scale factors must be swapped.
        float scaleX = (float) getWidth() / imageHeight;
        float scaleY = (float) getHeight() / imageWidth;

        // Correct transformation for 90-degree rotated portrait image
        // screen_x = img_y * scaleX
        // screen_y = (img_width - img_x) * scaleY
        int left = (int) (targetRect.top * scaleX);
        int top = (int) ((imageWidth - targetRect.right) * scaleY);
        int right = (int) (targetRect.bottom * scaleX);
        int bottom = (int) ((imageWidth - targetRect.left) * scaleY);

        drawRect.set(left, top, right, bottom);
        drawRect.inset(-10, -10); // Make it slightly larger than the text
        
        // Draw semi-transparent background
        canvas.drawRect(drawRect, fillPaint);

        float cornerSize = 40f;
        
        // Top-left corner
        canvas.drawLine(drawRect.left, drawRect.top, drawRect.left + cornerSize, drawRect.top, strokePaint);
        canvas.drawLine(drawRect.left, drawRect.top, drawRect.left, drawRect.top + cornerSize, strokePaint);

        // Top-right corner
        canvas.drawLine(drawRect.right, drawRect.top, drawRect.right - cornerSize, drawRect.top, strokePaint);
        canvas.drawLine(drawRect.right, drawRect.top, drawRect.right, drawRect.top + cornerSize, strokePaint);

        // Bottom-left corner
        canvas.drawLine(drawRect.left, drawRect.bottom, drawRect.left + cornerSize, drawRect.bottom, strokePaint);
        canvas.drawLine(drawRect.left, drawRect.bottom, drawRect.left, drawRect.bottom - cornerSize, strokePaint);

        // Bottom-right corner
        canvas.drawLine(drawRect.right, drawRect.bottom, drawRect.right - cornerSize, drawRect.bottom, strokePaint);
        canvas.drawLine(drawRect.right, drawRect.bottom, drawRect.right, drawRect.bottom - cornerSize, strokePaint);
    }
}
