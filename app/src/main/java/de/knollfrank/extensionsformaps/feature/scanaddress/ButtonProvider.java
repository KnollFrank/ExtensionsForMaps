package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.common.Booleans;
import de.knollfrank.extensionsformaps.common.Optionals;

class ButtonProvider {

    private final ResourceName viewId;
    private final String viewIdSubstring;
    private final String contentOrText;

    public ButtonProvider(final ResourceName viewId,
                          final String viewIdSubstring,
                          final String contentOrText) {
        this.viewId = viewId;
        this.viewIdSubstring = viewIdSubstring;
        this.contentOrText = contentOrText;
    }

    public Optional<AccessibilityNodeInfo> findButton(final AccessibilityNodeInfo root) {
        return Optionals
                .streamOfPresentElements(
                        () -> findButtonByViewId(root, viewId),
                        () -> findButtonByViewIdOrContentOrText(root, viewIdSubstring, contentOrText))
                .findFirst();
    }

    private static Optional<AccessibilityNodeInfo> findButtonByViewId(final AccessibilityNodeInfo root, final ResourceName viewId) {
        return new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId(viewId);
    }

    private static Optional<AccessibilityNodeInfo> findButtonByViewIdOrContentOrText(
            final AccessibilityNodeInfo root,
            final String viewIdSubstring,
            final String contentOrText) {
        return new AccessibilityNodeInfoWrapper(root)
                .streamPreOrder()
                .filter(
                        node ->
                                Booleans.or(
                                        () -> viewIdContainsSubstring(node, viewIdSubstring),
                                        () -> contentOrTextEqualsIgnoreCase(new AccessibilityNodeInfoWrapper(node), contentOrText)))
                .findFirst();
    }

    private static boolean viewIdContainsSubstring(final AccessibilityNodeInfo node, final String substring) {
        return new AccessibilityNodeInfoWrapper(node)
                .getViewIdResourceName()
                .filter(_viewId -> _viewId.contains(substring))
                .isPresent();
    }

    private static boolean contentOrTextEqualsIgnoreCase(final AccessibilityNodeInfoWrapper haystack, final String needle) {
        return Optionals
                .streamOfPresentElements(
                        haystack::getContentDescription,
                        haystack::getText)
                .anyMatch(contentOrText -> contentOrText.equalsIgnoreCase(needle));
    }
}
