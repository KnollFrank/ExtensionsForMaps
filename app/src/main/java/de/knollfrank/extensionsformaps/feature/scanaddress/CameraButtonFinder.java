package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;

class CameraButtonFinder {

    private final GoogleAppContext googleAppContext;

    public CameraButtonFinder(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findCameraButton(final AccessibilityNodeInfo root) {
        return createCameraButtonFinder().findButton(root);
    }

    private ButtonFinder createCameraButtonFinder() {
        return new ButtonFinder(
                ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_camera"),
                "aim_camera",
                googleAppContext.takePhotoText());
    }
}
