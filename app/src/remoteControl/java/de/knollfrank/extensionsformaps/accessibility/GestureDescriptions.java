package de.knollfrank.extensionsformaps.accessibility;

import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.GestureDescription.StrokeDescription;
import android.graphics.Path;
import android.graphics.Point;

class GestureDescriptions {

    public static GestureDescription getClickGesture(final Point point) {
        return new GestureDescription
                .Builder()
                .addStroke(getClickStroke(point))
                .build();
    }

    private static StrokeDescription getClickStroke(final Point point) {
        return new StrokeDescription(getClickPath(point), 0, 100);
    }

    private static Path getClickPath(final Point point) {
        final Path path = new Path();
        path.moveTo(point.x, point.y);
        return path;
    }
}
