package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;

class AIModeButtonFinder {

    private final GoogleAppContext googleAppContext;

    public AIModeButtonFinder(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findAIModeButton(final AccessibilityNodeInfo root) {
        return createAIModeButtonFinder().findButton(root);
    }

    private ButtonFinder createAIModeButtonFinder() {
        return new ButtonFinder(
                ResourceNameFactory.createGoogleAppResourceName("googleapp_sbn_aim_chip"),
                "aim_chip",
                googleAppContext.aiModeText());
    }
}
