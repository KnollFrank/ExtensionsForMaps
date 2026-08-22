package de.knollfrank.extensionsformaps.common;

import android.graphics.Path;
import android.graphics.Point;

public record PathWrapper(Path path) {

    public void moveTo(final Point point) {
        path.moveTo(point.x, point.y);
    }
}
