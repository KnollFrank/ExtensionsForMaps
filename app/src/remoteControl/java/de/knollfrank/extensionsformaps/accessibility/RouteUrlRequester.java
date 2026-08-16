package de.knollfrank.extensionsformaps.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.core.content.ContextCompat;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import de.knollfrank.extensionsformaps.common.Optionals;
import de.knollfrank.extensionsformaps.common.URLs;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrlFactory;

// FK-TODO: refactor
public class RouteUrlRequester {

    private static final String TAG = RouteUrlRequester.class.getSimpleName();
    private static final String SHARE_ID = "com.google.android.apps.maps:id/directions_header_share_action_button";

    @FunctionalInterface
    public interface RouteUrlCallback {

        void onRouteUrlExtracted(DirectionsUrl directionsUrl);
    }

    private final AccessibilityService service;
    private Optional<RouteUrlCallback> routeUrlCallback = Optional.empty();
    private boolean isWaitingToClickShareAfterBack = false;

    public RouteUrlRequester(final AccessibilityService service) {
        this.service = service;
    }

    public void requestRouteUrl(final RouteUrlCallback routeUrlCallback) {
        this.routeUrlCallback = Optional.of(routeUrlCallback);
        if (!tryClickShareButton()) {
            Log.d(TAG, "Share button not found. Dismissing overlay via BACK.");
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            isWaitingToClickShareAfterBack = true;
        }
    }

    public boolean isWaitingForUrl() {
        return routeUrlCallback.isPresent();
    }

    public void handleGoogleMapsEvent(final AccessibilityEvent event) {
        if (isWaitingToClickShareAfterBack && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            tryClickShareButton();
        }
    }

    public void handleResolverEvent() {
        Optionals.ifPresentBoth(
                routeUrlCallback,
                new AccessibilityServices(service).getRootInActiveWindow(),
                this::handleResolverEvent);
    }

    private void handleResolverEvent(final RouteUrlCallback routeUrlCallback,
                                     final AccessibilityNodeInfo rootNode) {
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
                CompletableFuture
                        .supplyAsync(
                                () ->
                                        DirectionsUrlFactory
                                                .createDirectionsUrl(URLs.createUrl(urlText.toString()))
                                                .orElseThrow())
                        .thenAcceptAsync(
                                directionsUrl -> {
                                    routeUrlCallback.onRouteUrlExtracted(directionsUrl);
                                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                                },
                                ContextCompat.getMainExecutor(service));
                this.routeUrlCallback = Optional.empty();
            }
        }
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
