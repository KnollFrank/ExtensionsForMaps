package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.google.common.collect.ImmutableList;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;

public class RouteUrlRequester {

    private static final String TAG = "RouteUrlRequester";
    private static final String SHARE_ID = "com.google.android.apps.maps:id/directions_header_share_action_button";

    @FunctionalInterface
    public interface RouteUrlCallback {

        void onUrlExtracted(URL url);
    }

    private final AccessibilityService service;
    private RouteUrlCallback currentCallback;
    private boolean isWaitingToClickShareAfterBack = false;

    public RouteUrlRequester(final AccessibilityService service) {
        this.service = service;
    }

    public void requestUrl(final RouteUrlCallback callback) {
        this.currentCallback = callback;
        if (!tryClickShareButton()) {
            Log.d(TAG, "Share button not found. Dismissing overlay via BACK.");
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            isWaitingToClickShareAfterBack = true;
        }
    }

    public void handleGoogleMapsEvent(final AccessibilityEvent event) {
        if (isWaitingToClickShareAfterBack && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            tryClickShareButton();
        }
    }

    public void handleResolverEvent() {
        if (currentCallback == null) {
            return;
        }

        final AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }

        final List<AccessibilityNodeInfo> urlNodes =
                ImmutableList
                        .<AccessibilityNodeInfo>builder()
                        .addAll(rootNode.findAccessibilityNodeInfosByViewId("android:id/content_preview_text"))
                        .addAll(rootNode.findAccessibilityNodeInfosByViewId("com.android.intentresolver:id/sem_chooser_sub_title_details_view"))
                        .build();

        if (!urlNodes.isEmpty()) {
            final CharSequence urlText = urlNodes.get(0).getText();
            if (urlText != null) {
                Log.d(TAG, "Extracted URL: " + urlText);
                final URL routeUrl = URLs.createUrl(urlText.toString());
                currentCallback.onUrlExtracted(routeUrl);
                currentCallback = null;
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }
            for (final AccessibilityNodeInfo n : urlNodes) {
                n.recycle();
            }
        }
        rootNode.recycle();
    }

    private boolean tryClickShareButton() {
        final Optional<AccessibilityNodeInfo> shareButton = findShareButtonInAllWindows();
        if (shareButton.isPresent()) {
            shareButton.get().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            isWaitingToClickShareAfterBack = false;
            Log.d(TAG, "Successfully clicked Share button.");
            return true;
        }
        return false;
    }

    private Optional<AccessibilityNodeInfo> findShareButtonInAllWindows() {
        final List<AccessibilityWindowInfo> windows = service.getWindows();
        for (final AccessibilityWindowInfo window : windows) {
            final AccessibilityNodeInfo root = window.getRoot();
            if (root != null) {
                final Optional<AccessibilityNodeInfo> button = findShareButton(root);
                if (button.isPresent()) {
                    return button;
                }
                root.recycle();
            }
        }
        return Optional.empty();
    }

    private Optional<AccessibilityNodeInfo> findShareButton(final AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(SHARE_ID);
        if (nodes.isEmpty()) {
            nodes = rootNode.findAccessibilityNodeInfosByText("Share");
        }
        if (nodes.isEmpty()) {
            nodes = rootNode.findAccessibilityNodeInfosByText("Teilen");
        }
        return nodes.isEmpty() ? Optional.empty() : Optional.of(nodes.get(0));
    }
}
