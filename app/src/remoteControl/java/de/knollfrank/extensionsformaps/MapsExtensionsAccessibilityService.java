package de.knollfrank.extensionsformaps;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import de.knollfrank.extensionsformaps.accessibility.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContextResolver;
import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester;
import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester.RouteUrlCallback;
import de.knollfrank.extensionsformaps.accessibility.StopCountDetector;
import de.knollfrank.extensionsformaps.feature.ActiveServiceHighlightFeature;
import de.knollfrank.extensionsformaps.feature.AddStopFeature;
import de.knollfrank.extensionsformaps.feature.CompoundFeature;
import de.knollfrank.extensionsformaps.feature.ScanAddressFeature;
import de.knollfrank.extensionsformaps.feature.SortFeature;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizerFactory;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class MapsExtensionsAccessibilityService extends AccessibilityService {

    private static final String TAG = MapsExtensionsAccessibilityService.class.getSimpleName();
    private static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";
    private static final String SYSTEM_PACKAGE = "android";
    private static final String GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox";
    private static final String GEMINI_APP_PACKAGE = "com.google.android.apps.bard";

    private CompoundFeature compoundFeature;
    private StopCountDetector stopCountDetector;
    private RouteUrlRequester urlRequester;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        urlRequester = new RouteUrlRequester(this);
        final SortFeature sortFeature = createSortFeature(urlRequester, this);
        final GoogleMapsContext googleMapsContext = GoogleMapsContextResolver.resolve(this);
        final AddStopFeature addStopFeature = createAddStopFeature(googleMapsContext, urlRequester, this);
        stopCountDetector =
                new StopCountDetector(
                        googleMapsContext,
                        List.of(sortFeature, addStopFeature));
        compoundFeature =
                new CompoundFeature(
                        List.of(
                                sortFeature,
                                addStopFeature,
                                new ActiveServiceHighlightFeature(this),
                                new ScanAddressFeature(this)));
        compoundFeature.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        Optional
                .ofNullable(event.getPackageName())
                .map(CharSequence::toString)
                .ifPresent(
                        packageName -> {
                            // WICHTIG: Wenn wir Google Maps verlassen, muessen Maps-Overlays sofort verschwinden.
                            // Das gilt auch, wenn wir zu Gemini oder in andere Systembereiche wechseln.
                            if (!GOOGLE_MAPS_PACKAGE.equals(packageName)) {
                                compoundFeature.reset();
                            }
                            switch (packageName) {
                                case GOOGLE_MAPS_PACKAGE:
                                    handleGoogleMapsEvent(event);
                                    break;
                                case GOOGLE_APP_PACKAGE:
                                case GEMINI_APP_PACKAGE:
                                    handleGoogleAppEvent(event);
                                    break;
                                case RESOLVER_PACKAGE:
                                case SYSTEM_PACKAGE:
                                    if (urlRequester.isWaitingForUrl()) {
                                        urlRequester.handleResolverEvent();
                                    }
                                    break;
                            }
                        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compoundFeature.onDestroy();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted.");
    }

    private static SortFeature createSortFeature(final RouteUrlRequester urlRequester,
                                                 final AccessibilityService service) {
        return new SortFeature(
                service,
                urlRequester,
                new RouteUrlCallback() {

                    @Override
                    public void onRouteUrlExtracted(final DirectionsUrl routeUrl) {
                        Log.d(TAG, "Extracted route URL for SORT: " + routeUrl);
                        final RouteOptimizationWorkflow routeOptimizationWorkflow =
                                new RouteOptimizationWorkflow(
                                        RouteOptimizerFactory.createRouteOptimizer(service),
                                        service);
                        routeOptimizationWorkflow.optimizeThenShowRoute(routeUrl);
                    }
                });
    }

    private static AddStopFeature createAddStopFeature(
            final GoogleMapsContext googleMapsContext,
            final RouteUrlRequester urlRequester,
            final AccessibilityService service) {
        final AtomicReference<AddStopFeature> addStopFeatureRef = new AtomicReference<>();
        addStopFeatureRef.set(
                new AddStopFeature(
                        service,
                        googleMapsContext,
                        urlRequester,
                        new RouteUrlRequester.RouteUrlCallback() {

                            @Override
                            public void onRouteUrlExtracted(final DirectionsUrl routeUrl) {
                                addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
                                        routeUrl,
                                        addStopFeatureRef.get(),
                                        service);
                            }
                        }));
        return addStopFeatureRef.get();
    }

    private static void addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
            final DirectionsUrl directionsUrl,
            final AddStopFeature addStopFeature,
            final AccessibilityService service) {
        final ProgressOverlay progressOverlay = new ProgressOverlay(service);
        progressOverlay.show();
        DummyStopAdder
                .addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
                        directionsUrl,
                        service)
                .thenRun(() -> {
                    progressOverlay.hide();
                    addStopFeature.startAutomation();
                });
    }

    private void handleGoogleMapsEvent(final AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            compoundFeature.reset();
        }
        urlRequester.handleGoogleMapsEvent(event);
        new AccessibilityServiceWrapper(this)
                .getRootInActiveWindow()
                .ifPresent(
                        root -> {
                            stopCountDetector.detect(root);
                            compoundFeature.onGoogleMapsEvent(event, root);
                        });
    }

    private void handleGoogleAppEvent(final AccessibilityEvent event) {
        new AccessibilityServiceWrapper(this)
                .getRootInActiveWindow()
                .ifPresent(root -> compoundFeature.onGoogleAppEvent(event, root));
    }
}
