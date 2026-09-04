package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;
import java.util.function.Predicate;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.common.Optionals;

class InputFieldProvider {

    private static final ResourceName AIM_INPUT_TEXT_ID = ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_autocomplete_text_input");

    private final Predicate<AccessibilityNodeInfo> classNameContainsEditText;
    private final GoogleAppContext googleAppContext;

    public InputFieldProvider(final Predicate<AccessibilityNodeInfo> classNameContainsEditText,
                              final GoogleAppContext googleAppContext) {
        this.classNameContainsEditText = classNameContainsEditText;
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findInputField(final AccessibilityNodeInfo root) {
        return Optionals
                .streamOfPresentElements(
                        () -> new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId(AIM_INPUT_TEXT_ID),
                        () -> findNodeByHintOrText(root, googleAppContext.askAnythingText()),
                        () -> findEditText(root))
                .findFirst();
    }

    // FK-TODO: refactor
    private static Optional<AccessibilityNodeInfo> findNodeByHintOrText(final AccessibilityNodeInfo root, final String needle) {
        return new AccessibilityNodeInfoWrapper(root)
                .streamPreOrder()
                .filter(node -> {
                    final AccessibilityNodeInfoWrapper wrapper = new AccessibilityNodeInfoWrapper(node);
                    return wrapper.getHintText().map(h -> h.contains(needle)).orElse(false)
                            || wrapper.getText().map(t -> t.contains(needle)).orElse(false);
                })
                .findFirst();
    }

    private Optional<AccessibilityNodeInfo> findEditText(final AccessibilityNodeInfo node) {
        return new AccessibilityNodeInfoWrapper(node)
                .streamPreOrder()
                .filter(classNameContainsEditText)
                .findFirst();
    }
}
