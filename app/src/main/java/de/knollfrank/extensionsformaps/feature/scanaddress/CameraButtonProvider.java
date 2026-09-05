package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.common.Booleans;
import de.knollfrank.extensionsformaps.common.Optionals;

class CameraButtonProvider {

    private static final ResourceName AIM_CAMERA_ID = ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_camera");

    private final GoogleAppContext googleAppContext;

    public CameraButtonProvider(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findCameraButton(final AccessibilityNodeInfo root) {
        return Optionals
                .streamOfPresentElements(
                        () -> findCameraButtonByResourceId(root),
                        () -> findCameraButtonByViewIdOrContentOrText(root))
                .findFirst();
    }

    private static Optional<AccessibilityNodeInfo> findCameraButtonByResourceId(final AccessibilityNodeInfo root) {
        return new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId(AIM_CAMERA_ID);
    }

    private Optional<AccessibilityNodeInfo> findCameraButtonByViewIdOrContentOrText(final AccessibilityNodeInfo root) {
        return new AccessibilityNodeInfoWrapper(root)
                .streamPreOrder()
                .filter(
                        node ->
                                Booleans.or(
                                        () -> viewIdContainsAimCamera(node),
                                        () -> contentOrTextEqualsIgnoreCase(new AccessibilityNodeInfoWrapper(node), googleAppContext.takePhotoText())))
                .findFirst();
    }

    private static boolean viewIdContainsAimCamera(final AccessibilityNodeInfo node) {
        return new AccessibilityNodeInfoWrapper(node)
                .getViewIdResourceName()
                .filter(_viewId -> _viewId.contains("aim_camera"))
                .isPresent();
    }

    private static boolean contentOrTextEqualsIgnoreCase(final AccessibilityNodeInfoWrapper haystack, final String needle) {
        return Optionals
                .streamOfPresentElements(
                        haystack::getContentDescription,
                        haystack::getText)
                .anyMatch(contentOrText -> contentOrText.equalsIgnoreCase(needle));
    }
}
