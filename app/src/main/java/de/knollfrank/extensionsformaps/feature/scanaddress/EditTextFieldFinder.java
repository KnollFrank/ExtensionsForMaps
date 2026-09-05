package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;

class EditTextFieldFinder {

    private static final ResourceName SEARCH_EDIT_TEXT_ID = ResourceNameFactory.createGoogleMapsResourceName("search_omnibox_edit_text");

    public static Optional<AccessibilityNodeInfo> findEditTextField(final AccessibilityNodeInfo root) {
        return new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId(SEARCH_EDIT_TEXT_ID);
    }
}
