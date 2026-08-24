package de.knollfrank.extensionsformaps.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.common.collect.ImmutableList;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        void onRouteUrlExtracted(DirectionsUrl routeUrl);
    }

    private final AccessibilityService accessibilityService;
    private Optional<RouteUrlCallback> routeUrlCallback = Optional.empty();
    private boolean isWaitingToClickShareAfterBack = false;

    public RouteUrlRequester(final AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
    }

    public void requestRouteUrl(final RouteUrlCallback routeUrlCallback) {
        this.routeUrlCallback = Optional.of(routeUrlCallback);
        if (!tryClickShareButton()) {
            Log.d(TAG, "Share button not found. Dismissing overlay via BACK.");
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
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
                Pair.create(
                        routeUrlCallback,
                        new AccessibilityServiceWrapper(accessibilityService).getRootInActiveWindow()),
                this::handleResolverEvent);
    }

    private void handleResolverEvent(final RouteUrlCallback routeUrlCallback,
                                     final AccessibilityNodeInfo rootNode) {
        RouteUrlRequester
                .getUrl(rootNode)
                .ifPresent(
                        url -> {
                            Log.d(TAG, "Extracted URL: " + url);
                            DirectionsUrlFactory
                                    .createDirectionsUrl(url)
                                    .thenApply(Optional::orElseThrow)
                                    .thenAcceptAsync(
                                            directionsUrl -> {
                                                routeUrlCallback.onRouteUrlExtracted(directionsUrl);
                                                accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                                            },
                                            ContextCompat.getMainExecutor(accessibilityService));
                            this.routeUrlCallback = Optional.empty();
                        });
    }

    private static Optional<URL> getUrl(final AccessibilityNodeInfo rootNode) {
        return RouteUrlRequester
                .getUrlNodes(rootNode)
                .stream()
                .map(AccessibilityNodeInfo::getText)
                .filter(Objects::nonNull)
                .findFirst()
                .map(CharSequence::toString)
                .map(URLs::createUrl);
    }

    private static List<AccessibilityNodeInfo> getUrlNodes(final AccessibilityNodeInfo rootNode) {
        return ImmutableList
                .<AccessibilityNodeInfo>builder()
                .addAll(rootNode.findAccessibilityNodeInfosByViewId("android:id/content_preview_text"))
                .addAll(rootNode.findAccessibilityNodeInfosByViewId("com.android.intentresolver:id/sem_chooser_sub_title_details_view"))
                .build();
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
        return accessibilityService
                .getWindows()
                .stream()
                .flatMap(window -> Optional.ofNullable(window.getRoot()).stream())
                .flatMap(root -> findShareButton(root).stream())
                .findFirst();
    }

    // FK-TODO: refactor, i18n for "Share" and "Teilen" by using a key
    private Optional<AccessibilityNodeInfo> findShareButton(final AccessibilityNodeInfo rootNode) {
        return Optionals
                .streamOfPresentElements(
                        () -> findFirst(rootNode.findAccessibilityNodeInfosByViewId(SHARE_ID)),
                        () -> findFirst(rootNode.findAccessibilityNodeInfosByText("Share")),
                        () -> findFirst(rootNode.findAccessibilityNodeInfosByText("Teilen")))
                .findFirst();
    }

    private static Optional<AccessibilityNodeInfo> findFirst(final List<AccessibilityNodeInfo> nodes) {
        return nodes.stream().findFirst();
    }
}
