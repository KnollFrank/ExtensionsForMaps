package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;
import java.util.function.Predicate;

class AddressProvider {

    private final Predicate<AccessibilityNodeInfo> classNameContainsEditText;

    public AddressProvider(final Predicate<AccessibilityNodeInfo> classNameContainsEditText) {
        this.classNameContainsEditText = classNameContainsEditText;
    }

    public Optional<String> getAddress(final AccessibilityNodeInfo root) {
        return AIPrompt.extractAddressFromAIResponse(
                new VisibleResponseTextProvider(classNameContainsEditText)
                        .collectVisibleResponseText(root));
    }
}
