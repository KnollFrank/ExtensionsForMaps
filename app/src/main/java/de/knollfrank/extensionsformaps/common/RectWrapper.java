package de.knollfrank.extensionsformaps.common;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.Optional;

public record RectWrapper(Rect rect) {

    public Optional<Point> getCenter() {
        return rect.isEmpty() ?
                Optional.empty() :
                Optional.of(new Point(rect.centerX(), rect.centerY()));
    }

    public boolean contains(final Point point) {
        return rect.contains(point.x, point.y);
    }
}
