package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;

class CameraButtonProvider {

    private final GoogleAppContext googleAppContext;

    public CameraButtonProvider(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findCameraButton(final AccessibilityNodeInfo root) {
        return createCameraButtonProvider().findButton(root);
    }

    private ButtonProvider createCameraButtonProvider() {
        return new ButtonProvider(
                ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_camera"),
                "aim_camera",
                googleAppContext.takePhotoText());
    }
}
