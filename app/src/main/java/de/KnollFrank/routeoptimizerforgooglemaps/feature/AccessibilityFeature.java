package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public interface AccessibilityFeature {

    default void onMapsEvent(AccessibilityEvent event, AccessibilityNodeInfo root) {
    }

    default void onServiceConnected() {
    }

    default void onDestroy() {
    }
}
