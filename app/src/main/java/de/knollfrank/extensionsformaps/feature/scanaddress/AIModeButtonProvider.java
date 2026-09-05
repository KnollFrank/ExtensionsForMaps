package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;

class AIModeButtonProvider {

    private final GoogleAppContext googleAppContext;

    public AIModeButtonProvider(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findAIModeButton(final AccessibilityNodeInfo root) {
        return createAIModeButtonProvider().findButton(root);
    }

    private ButtonProvider createAIModeButtonProvider() {
        return new ButtonProvider(
                ResourceNameFactory.createGoogleAppResourceName("googleapp_sbn_aim_chip"),
                "aim_chip",
                googleAppContext.aiModeText());
    }
}
