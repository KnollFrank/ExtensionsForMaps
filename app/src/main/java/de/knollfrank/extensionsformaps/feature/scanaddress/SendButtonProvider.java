package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;

class SendButtonProvider {

    private final GoogleAppContext googleAppContext;

    public SendButtonProvider(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findSendButton(final AccessibilityNodeInfo root) {
        return this
                .createSendButtonProvider()
                .findButton(root)
                .filter(AccessibilityNodeInfo::isEnabled);
    }

    private ButtonProvider createSendButtonProvider() {
        return new ButtonProvider(
                ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_enter_button"),
                "aim_enter_button",
                googleAppContext.sendText());
    }
}
