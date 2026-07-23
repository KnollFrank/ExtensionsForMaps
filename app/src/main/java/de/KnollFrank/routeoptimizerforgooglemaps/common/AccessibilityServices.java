package de.KnollFrank.routeoptimizerforgooglemaps.common;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

public class AccessibilityServices {

    private AccessibilityServices() {
    }

    public static Optional<AccessibilityNodeInfo> getRootInActiveWindow(final AccessibilityService accessibilityService) {
        return Optional.ofNullable(accessibilityService.getRootInActiveWindow());
    }
}
