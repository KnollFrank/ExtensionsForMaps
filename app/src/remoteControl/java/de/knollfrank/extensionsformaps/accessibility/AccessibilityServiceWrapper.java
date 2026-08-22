package de.knollfrank.extensionsformaps.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.common.RectWrapper;

public class AccessibilityServiceWrapper {

    private final AccessibilityService accessibilityService;

    public AccessibilityServiceWrapper(final AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
    }

    public static AccessibilityServiceWrapper of(final AccessibilityService accessibilityService) {
        return new AccessibilityServiceWrapper(accessibilityService);
    }

    public Optional<AccessibilityNodeInfo> getRootInActiveWindow() {
        return Optional.ofNullable(accessibilityService.getRootInActiveWindow());
    }

    public boolean click(final AccessibilityNodeInfo node) {
        return click(AccessibilityNodeInfoWrapper.of(node).getBoundsInScreen());
    }

    public boolean click(final Rect bounds) {
        return RectWrapper
                .of(bounds)
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
