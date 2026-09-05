package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;

class SendButtonFinder {

    private final GoogleAppContext googleAppContext;

    public SendButtonFinder(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findSendButton(final AccessibilityNodeInfo root) {
        return this
                .createSendButtonFinder()
                .findButton(root)
                .filter(AccessibilityNodeInfo::isEnabled);
    }

    private ButtonFinder createSendButtonFinder() {
        return new ButtonFinder(
                ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_enter_button"),
                "aim_enter_button",
                googleAppContext.sendText());
    }
}
