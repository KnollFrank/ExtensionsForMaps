package de.knollfrank.extensionsformaps.accessibility.wrapper;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.common.Lists;

public record AccessibilityNodeInfoWrapper(AccessibilityNodeInfo node) {

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(final ResourceName viewId) {
        return node.findAccessibilityNodeInfosByViewId(viewId.getFullyQualifiedName());
    }

    public Optional<AccessibilityNodeInfo> findFirstAccessibilityNodeInfoByViewId(final ResourceName viewId) {
        return Lists.findFirst(findAccessibilityNodeInfosByViewId(viewId));
    }

    public Optional<AccessibilityNodeInfo> findFirstAccessibilityNodeInfoByText(final String text) {
        return Lists.findFirst(node.findAccessibilityNodeInfosByText(text));
    }

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

    public Optional<String> getText() {
        return Optional
                .ofNullable(node.getText())
                .map(CharSequence::toString);
    }

    public Optional<String> getPackageName() {
        return Optional
                .ofNullable(node.getPackageName())
                .map(CharSequence::toString);
    }
}
