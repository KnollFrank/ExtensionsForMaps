package de.knollfrank.extensionsformaps.accessibility;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.RESOLVER_PACKAGE;
import static de.knollfrank.extensionsformaps.accessibility.PackageNames.SYSTEM_PACKAGE;

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

import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.common.Optionals;
import de.knollfrank.extensionsformaps.common.URLs;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrlFactory;

public class RouteUrlRequester {

    private static final String TAG = RouteUrlRequester.class.getSimpleName();
    private static final ResourceName SHARE_ID = ResourceNameFactory.createGoogleMapsResourceName("directions_header_share_action_button");

    @FunctionalInterface
    public interface RouteUrlCallback {

        void onRouteUrlExtracted(DirectionsUrl routeUrl);
    }

    private final AccessibilityService accessibilityService;
    private final GoogleMapsContext googleMapsContext;
    private Optional<RouteUrlCallback> routeUrlCallback = Optional.empty();
    private boolean isWaitingToClickShareAfterBack = false;

    public RouteUrlRequester(final AccessibilityService accessibilityService,
                             final GoogleMapsContext googleMapsContext) {
        this.accessibilityService = accessibilityService;
        this.googleMapsContext = googleMapsContext;
    }

    public void requestRouteUrl(final RouteUrlCallback routeUrlCallback) {
        this.routeUrlCallback = Optional.of(routeUrlCallback);
        if (!tryClickShareButton()) {
            Log.d(TAG, "Share button not found. Dismissing overlay via BACK.");
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            isWaitingToClickShareAfterBack = true;
        }
    }

    public void handleGoogleMapsEvent(final AccessibilityEvent event) {
        if (isWaitingToClickShareAfterBack && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            tryClickShareButton();
        }
    }

    public void tryExtractUrlFromShareSheetAndDeliverToCallback() {
        Optionals.ifPresentBoth(
                Pair.create(
                        new AccessibilityServiceWrapper(accessibilityService).getRootInActiveWindow(),
                        routeUrlCallback),
                this::extractUrlFromShareSheetAndDeliverToCallback);
    }

    private void extractUrlFromShareSheetAndDeliverToCallback(final AccessibilityNodeInfo shareSheet,
                                                              final RouteUrlCallback routeUrlCallback) {
        RouteUrlRequester
                .extractUrl(shareSheet)
                .ifPresent(
                        url -> {
                            Log.d(TAG, "Extracted URL: " + url);
                            deliverUrlToCallbackAndDismissShareSheet(url, routeUrlCallback);
                        });
    }

    private static Optional<URL> extractUrl(final AccessibilityNodeInfo rootNode) {
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
        return getUrlNodes(new AccessibilityNodeInfoWrapper(rootNode));
    }

    private static ImmutableList<AccessibilityNodeInfo> getUrlNodes(final AccessibilityNodeInfoWrapper rootNode) {
        return ImmutableList
                .<AccessibilityNodeInfo>builder()
                .addAll(rootNode.findAccessibilityNodeInfosByViewId(new ResourceName(SYSTEM_PACKAGE, "content_preview_text")))
                .addAll(rootNode.findAccessibilityNodeInfosByViewId(new ResourceName(RESOLVER_PACKAGE, "sem_chooser_sub_title_details_view")))
                .build();
    }

    private void deliverUrlToCallbackAndDismissShareSheet(final URL url,
                                                          final RouteUrlCallback routeUrlCallback) {
        this.routeUrlCallback = Optional.empty();
        DirectionsUrlFactory
                .createDirectionsUrl(url)
                .thenApply(Optional::orElseThrow)
                .thenAcceptAsync(
                        directionsUrl -> deliverUrlToCallbackAndDismissShareSheet(directionsUrl, routeUrlCallback),
                        ContextCompat.getMainExecutor(accessibilityService));
    }

    private void deliverUrlToCallbackAndDismissShareSheet(final DirectionsUrl directionsUrl,
                                                          final RouteUrlCallback routeUrlCallback) {
        routeUrlCallback.onRouteUrlExtracted(directionsUrl);
        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
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

    private Optional<AccessibilityNodeInfo> findShareButton(final AccessibilityNodeInfo rootNode) {
        final AccessibilityNodeInfoWrapper rootNodeWrapper = new AccessibilityNodeInfoWrapper(rootNode);
        return Optionals
                .streamOfPresentElements(
                        () -> rootNodeWrapper.findFirstAccessibilityNodeInfoByViewId(SHARE_ID),
                        () -> rootNodeWrapper.findFirstAccessibilityNodeInfoByText(googleMapsContext.shareText))
                .findFirst();
    }
}
