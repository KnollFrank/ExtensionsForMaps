package de.knollfrank.extensionsformaps.accessibility;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

public class AccessibilityNodeInfoWrapper {

    private final AccessibilityNodeInfo node;

    public AccessibilityNodeInfoWrapper(final AccessibilityNodeInfo node) {
        this.node = node;
    }

    public static AccessibilityNodeInfoWrapper of(final AccessibilityNodeInfo node) {
        return new AccessibilityNodeInfoWrapper(node);
    }

    public Rect getBoundsInScreen() {
        final Rect outBounds = new Rect();
        node.getBoundsInScreen(outBounds);
        return outBounds;
    }
}
