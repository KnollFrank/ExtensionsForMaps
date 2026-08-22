package de.knollfrank.extensionsformaps.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.common.RectWrapper;

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
        return RectWrapper
                .of(bounds)
                .getCenter()
                .map(this::click)
                .orElse(false);
    }

    public static Rect getBoundsInScreen(final AccessibilityNodeInfo node) {
        final Rect outBounds = new Rect();
        node.getBoundsInScreen(outBounds);
        return outBounds;
    }

    private boolean click(final Point point) {
        return accessibilityService.dispatchGesture(
                GestureDescriptions.getClickGesture(point),
                null,
                null);
    }
}
