package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.common.Optionals;

class VisibleResponseTextProvider {

    private final Predicate<AccessibilityNodeInfo> classNameContainsEditText;

    public VisibleResponseTextProvider(final Predicate<AccessibilityNodeInfo> classNameContainsEditText) {
        this.classNameContainsEditText = classNameContainsEditText;
    }

    public String collectVisibleResponseText(final AccessibilityNodeInfo root) {
        return new AccessibilityNodeInfoWrapper(root)
                .streamPreOrder()
                .filter(node -> classNameContainsEditText.negate().test(node))
                .map(VisibleResponseTextProvider::getConcatenatedTextAndContentDescription)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private static String getConcatenatedTextAndContentDescription(final AccessibilityNodeInfo node) {
        final AccessibilityNodeInfoWrapper nodeWrapper = new AccessibilityNodeInfoWrapper(node);
        return Optionals
                .streamOfPresentElements(
                        nodeWrapper::getText,
                        nodeWrapper::getContentDescription)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining(" "));
    }
}
