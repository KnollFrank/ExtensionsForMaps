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

    private final Paint paint = new Paint();
    private Rect targetRect = null;
    private int imageWidth = 0;
    private int imageHeight = 0;

    public AddressOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.parseColor("#4285F4")); // Google Blue
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
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

        // Scale factors
        float scaleX = (float) getWidth() / imageHeight; // ML Kit image is rotated 90 deg usually
        float scaleY = (float) getHeight() / imageWidth;

        // Transform image coordinates to view coordinates
        // Assuming 90 degree rotation (portrait mode)
        int left = (int) (targetRect.top * scaleY);
        int top = (int) ((imageHeight - targetRect.right) * scaleX);
        int right = (int) (targetRect.bottom * scaleY);
        int bottom = (int) ((imageHeight - targetRect.left) * scaleX);

        float cornerSize = 40f;
        
        // Top-left corner
        canvas.drawLine(left, top, left + cornerSize, top, paint);
        canvas.drawLine(left, top, left, top + cornerSize, paint);

        // Top-right corner
        canvas.drawLine(right, top, right - cornerSize, top, paint);
        canvas.drawLine(right, top, right, top + cornerSize, paint);

        // Bottom-left corner
        canvas.drawLine(left, bottom, left + cornerSize, bottom, paint);
        canvas.drawLine(left, bottom, left, bottom - cornerSize, paint);

        // Bottom-right corner
        canvas.drawLine(right, bottom, right - cornerSize, bottom, paint);
        canvas.drawLine(right, bottom, right, bottom - cornerSize, paint);
    }
}
