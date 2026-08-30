package de.knollfrank.extensionsformaps;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GEMINI_APP_PACKAGE;
import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_APP_PACKAGE;
import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_MAPS_PACKAGE;
import static de.knollfrank.extensionsformaps.accessibility.PackageNames.RESOLVER_PACKAGE;
import static de.knollfrank.extensionsformaps.accessibility.PackageNames.SYSTEM_PACKAGE;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContextResolver;
import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester;
import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester.RouteUrlCallback;
import de.knollfrank.extensionsformaps.accessibility.StopCountDetector;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityEventWrapper;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.feature.ActiveServiceHighlightFeature;
import de.knollfrank.extensionsformaps.feature.CompoundFeature;
import de.knollfrank.extensionsformaps.feature.addstop.AddStopFeature;
import de.knollfrank.extensionsformaps.feature.scanaddress.ScanAddressFeature;
import de.knollfrank.extensionsformaps.feature.sort.SortFeature;
import de.knollfrank.extensionsformaps.feature.sort.SortFeatureFactory;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizerFactory;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class MapsExtensionsAccessibilityService extends AccessibilityService {

    private static final String TAG = MapsExtensionsAccessibilityService.class.getSimpleName();

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
        new AccessibilityEventWrapper(event)
                .getPackageName()
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
                                    urlRequester.tryExtractUrlFromShareSheetAndDeliverToCallback();
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
                                                 final AccessibilityService accessibilityService) {
        return SortFeatureFactory.createSortFeature(
                urlRequester,
                new RouteUrlCallback() {

                    @Override
                    public void onRouteUrlExtracted(final DirectionsUrl routeUrl) {
                        Log.d(TAG, "Extracted route URL for SORT: " + routeUrl);
                        final RouteOptimizationWorkflow routeOptimizationWorkflow =
                                new RouteOptimizationWorkflow(
                                        RouteOptimizerFactory.createRouteOptimizer(accessibilityService),
                                        accessibilityService);
                        routeOptimizationWorkflow.optimizeThenShowRoute(routeUrl);
                    }
                },
                accessibilityService);
    }

    private static AddStopFeature createAddStopFeature(
            final GoogleMapsContext googleMapsContext,
            final RouteUrlRequester urlRequester,
            final AccessibilityService accessibilityService) {
        final AtomicReference<AddStopFeature> addStopFeatureRef = new AtomicReference<>();
        addStopFeatureRef.set(
                new AddStopFeature(
                        accessibilityService,
                        googleMapsContext,
                        urlRequester,
                        new RouteUrlRequester.RouteUrlCallback() {

                            @Override
                            public void onRouteUrlExtracted(final DirectionsUrl routeUrl) {
                                addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
                                        routeUrl,
                                        addStopFeatureRef.get(),
                                        accessibilityService);
                            }
                        }));
        return addStopFeatureRef.get();
    }

    private static void addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
            final DirectionsUrl directionsUrl,
            final AddStopFeature addStopFeature,
            final AccessibilityService accessibilityService) {
        final ProgressOverlay progressOverlay = new ProgressOverlay(accessibilityService);
        progressOverlay.show();
        DummyStopAdder
                .addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
                        directionsUrl,
                        accessibilityService)
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
                            stopCountDetector.detectStopCount(root);
                            compoundFeature.onGoogleMapsEvent(event, root);
                        });
    }

    private void handleGoogleAppEvent(final AccessibilityEvent event) {
        new AccessibilityServiceWrapper(this)
                .getRootInActiveWindow()
                .ifPresent(root -> compoundFeature.onGoogleAppEvent(event, root));
    }
}
