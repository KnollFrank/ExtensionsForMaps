package de.knollfrank.extensionsformaps.accessibility;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

public record AccessibilityNodeInfoWrapper(AccessibilityNodeInfo node) {

    public Rect getBoundsInScreen() {
        final Rect outBounds = new Rect();
        node.getBoundsInScreen(outBounds);
        return outBounds;
    }

    public Optional<AccessibilityNodeInfo> getLastChild() {
        final int childCount = node.getChildCount();
        return childCount > 0 ?
                Optional.ofNullable(node.getChild(childCount - 1)) :
                Optional.empty();
    }
}
