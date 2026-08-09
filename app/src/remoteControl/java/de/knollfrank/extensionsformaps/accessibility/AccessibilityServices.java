package de.knollfrank.extensionsformaps.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

public class AccessibilityServices {

    private final AccessibilityService accessibilityService;

    public AccessibilityServices(final AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
    }

    public Optional<AccessibilityNodeInfo> getRootInActiveWindow() {
        return Optional.ofNullable(accessibilityService.getRootInActiveWindow());
    }

    public boolean click(final AccessibilityNodeInfo node) {
        return click(getBoundsInScreen(node));
    }

    public boolean click(final Rect bounds) {
        return AccessibilityServices
                .getCenter(bounds)
                .map(this::click)
                .orElse(false);
    }

    // FK-TODO: verwende diese Methode an analogen Stellen
    private static Rect getBoundsInScreen(final AccessibilityNodeInfo node) {
        final Rect outBounds = new Rect();
        node.getBoundsInScreen(outBounds);
        return outBounds;
    }

    private static Optional<Point> getCenter(final Rect bounds) {
        if (bounds.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                new Point(
                        bounds.centerX(),
                        bounds.centerY()));
    }

    private boolean click(final Point point) {
        return accessibilityService.dispatchGesture(
                GestureDescriptions.getClickGesture(point),
                null,
                null);
    }
}
