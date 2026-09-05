package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.common.Optionals;

class CameraButtonProvider {

    private static final ResourceName AIM_CAMERA_ID = ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_camera");

    private final GoogleAppContext googleAppContext;

    public CameraButtonProvider(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    // FK-TODO: refactor
    public Optional<AccessibilityNodeInfo> findCameraButton(final AccessibilityNodeInfo root) {
        return Optionals
                .streamOfPresentElements(
                        () ->
                                // 1. Primäre Suche nach Resource-ID (searchbox_aim_camera)
                                new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId(AIM_CAMERA_ID),
                        () ->
                                // 2. Sekundäre Suche nach View-ID-Teilstring oder Content-Description / Text
                                new AccessibilityNodeInfoWrapper(root)
                                        .streamPreOrder()
                                        .filter(
                                                node -> {
                                                    final AccessibilityNodeInfoWrapper nodeWrapper = new AccessibilityNodeInfoWrapper(node);
                                                    final Optional<String> viewId = nodeWrapper.getViewIdResourceName();
                                                    if (viewId.isPresent() && viewId.orElseThrow().contains("aim_camera")) {
                                                        return true;
                                                    }
                                                    final String contentDesc = nodeWrapper.getContentDescription().orElse("");
                                                    final String text = nodeWrapper.getText().orElse("");
                                                    return contentDesc.equalsIgnoreCase(googleAppContext.takePhotoText())
                                                            || text.equalsIgnoreCase(googleAppContext.takePhotoText());
                                                })
                                        .findFirst())
                .findFirst();
    }
}
