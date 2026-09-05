package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;

import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;

class ButtonClick {

    private final AccessibilityService accessibilityService;

    public ButtonClick(final AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
    }

    public boolean clickButton(final AccessibilityNodeInfo button) {
        final AccessibilityNodeInfo ancestorButton =
                new AccessibilityNodeInfoWrapper(button)
                        .findClickableAncestor()
                        .orElse(button);
        return
                ancestorButton.performAction(AccessibilityNodeInfo.ACTION_CLICK) ||
                        new AccessibilityServiceWrapper(accessibilityService).click(ancestorButton) ||
                        new AccessibilityServiceWrapper(accessibilityService).click(button);
    }
}
