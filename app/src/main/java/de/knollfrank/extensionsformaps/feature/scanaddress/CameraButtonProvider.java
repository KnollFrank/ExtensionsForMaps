package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;

class CameraButtonProvider {

    private static final ResourceName AIM_CAMERA_ID = ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_camera");

    private final GoogleAppContext googleAppContext;

    public CameraButtonProvider(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    // FK-TODO: refactor
    public Optional<AccessibilityNodeInfo> findCameraButton(final AccessibilityNodeInfo root) {
        final AccessibilityNodeInfoWrapper wrapper = new AccessibilityNodeInfoWrapper(root);

        // 1. Primäre Suche nach Resource-ID (searchbox_aim_camera)
        final Optional<AccessibilityNodeInfo> byId = wrapper.findFirstAccessibilityNodeInfoByViewId(AIM_CAMERA_ID);
        if (byId.isPresent()) {
            return byId;
        }

        // 2. Sekundäre Suche nach View-ID-Teilstring oder Content-Description / Text
        return wrapper
                .streamPreOrder()
                .filter(
                        node -> {
                            final AccessibilityNodeInfoWrapper nodeWrapper = new AccessibilityNodeInfoWrapper(node);
                            final String viewId = node.getViewIdResourceName();
                            if (viewId != null && viewId.contains("aim_camera")) {
                                return true;
                            }
                            final String contentDesc = nodeWrapper.getContentDescription().orElse("");
                            final String text = nodeWrapper.getText().orElse("");
                            return contentDesc.equalsIgnoreCase(googleAppContext.takePhotoText())
                                    || text.equalsIgnoreCase(googleAppContext.takePhotoText());
                        })
                .findFirst();
    }
}
