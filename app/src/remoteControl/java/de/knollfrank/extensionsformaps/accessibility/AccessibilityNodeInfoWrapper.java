package de.knollfrank.extensionsformaps.accessibility;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

public record AccessibilityNodeInfoWrapper(AccessibilityNodeInfo node) {

    public Rect getBoundsInScreen() {
        final Rect outBounds = new Rect();
        node.getBoundsInScreen(outBounds);
        return outBounds;
    }
}
