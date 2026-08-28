package de.knollfrank.extensionsformaps.accessibility.wrapper;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.common.RectWrapper;

public record AccessibilityServiceWrapper(AccessibilityService accessibilityService) {

    public Optional<AccessibilityNodeInfo> getRootInActiveWindow() {
        return Optional.ofNullable(accessibilityService.getRootInActiveWindow());
    }

    public boolean click(final AccessibilityNodeInfo node) {
        return click(new AccessibilityNodeInfoWrapper(node).getBoundsInScreen());
    }

    public boolean click(final Rect bounds) {
        return new RectWrapper(bounds)
                .getCenter()
                .map(this::click)
                .orElse(false);
    }

    private boolean click(final Point point) {
        return accessibilityService.dispatchGesture(
                GestureDescriptions.getClickGesture(point),
                null,
                null);
    }
}
