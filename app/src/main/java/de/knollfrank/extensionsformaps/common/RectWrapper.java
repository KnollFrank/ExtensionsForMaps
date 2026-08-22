package de.knollfrank.extensionsformaps.common;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.Optional;

public class RectWrapper {

    private final Rect rect;

    private RectWrapper(final Rect rect) {
        this.rect = rect;
    }

    public static RectWrapper of(final Rect rect) {
        return new RectWrapper(rect);
    }

    public Optional<Point> getCenter() {
        return rect.isEmpty() ?
                Optional.empty() :
                Optional.of(new Point(rect.centerX(), rect.centerY()));
    }

    public boolean contains(final Point point) {
        return rect.contains(point.x, point.y);
    }
}
