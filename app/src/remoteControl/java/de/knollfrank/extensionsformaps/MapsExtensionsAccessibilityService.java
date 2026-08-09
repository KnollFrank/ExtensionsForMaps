package de.knollfrank.extensionsformaps;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.AccessibilityServices;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContextResolver;
import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester;
import de.knollfrank.extensionsformaps.accessibility.StopCountDetector;
import de.knollfrank.extensionsformaps.feature.AccessibilityFeature;
import de.knollfrank.extensionsformaps.feature.ActiveServiceHighlightFeature;
import de.knollfrank.extensionsformaps.feature.AddStopFeature;
import de.knollfrank.extensionsformaps.feature.ScanAddressFeature;
import de.knollfrank.extensionsformaps.feature.SortFeature;
import de.knollfrank.extensionsformaps.optimize.HaversineVehicleRoutingTransportCostsProvider;
import de.knollfrank.extensionsformaps.optimize.OsrmVehicleRoutingTransportCostsProvider;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizer;
import de.knollfrank.extensionsformaps.optimize.VehicleRoutingTransportCostsProvider;
import de.knollfrank.extensionsformaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

public class MapsExtensionsAccessibilityService extends AccessibilityService {

    private static final String TAG = MapsExtensionsAccessibilityService.class.getSimpleName();
    private static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";
    private static final String SYSTEM_PACKAGE = "android";
    private static final String GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox";
    private static final String GEMINI_APP_PACKAGE = "com.google.android.apps.bard";

    private List<AccessibilityFeature> features = List.of();
    private StopCountDetector stopCountDetector;
    private RouteUrlRequester urlRequester;
    private ActiveServiceHighlightFeature activeServiceHighlightFeature;
    // FK-TODO: Field can be converted to a local variable
    private ScanAddressFeature scanAddressFeature;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        urlRequester = new RouteUrlRequester(this);
        final GoogleMapsContext googleMapsContext = GoogleMapsContextResolver.resolve(this);

        final AddStopFeature[] addStopFeatureWrapper = new AddStopFeature[1];
        addStopFeatureWrapper[0] =
                new AddStopFeature(
                        this,
                        googleMapsContext,
                        urlRequester,
                        new RouteUrlRequester.RouteUrlCallback() {

                            @Override
                            public void onRouteUrlExtracted(final URL routeUrl) {
                                final ProgressOverlay progressOverlay = new ProgressOverlay(MapsExtensionsAccessibilityService.this);
                                progressOverlay.show();
                                DummyStopAdder
                                        .addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
                                                routeUrl,
                                                MapsExtensionsAccessibilityService.this)
                                        .thenRun(() -> {
                                            progressOverlay.hide();
                                            addStopFeatureWrapper[0].startAutomation();
                                        });
                            }
                        });
        final AddStopFeature addStopFeature = addStopFeatureWrapper[0];

        final SortFeature sortFeature =
                new SortFeature(
                        this,
                        urlRequester,
                        routeUrl -> {
                            Log.d(TAG, "Extracted route URL for SORT: " + routeUrl);
                            final RouteOptimizationWorkflow routeOptimizationWorkflow =
                                    new RouteOptimizationWorkflow(
                                            new RouteOptimizer(getVehicleRoutingTransportCostsProvider()),
                                            MapsExtensionsAccessibilityService.this);
                            routeOptimizationWorkflow.optimizeThenShowRoute(routeUrl);
                        });
        activeServiceHighlightFeature = new ActiveServiceHighlightFeature(this);
        scanAddressFeature = new ScanAddressFeature(this);
        stopCountDetector =
                new StopCountDetector(
                        googleMapsContext,
                        List.of(addStopFeature, sortFeature));
        features = List.of(addStopFeature, sortFeature, activeServiceHighlightFeature, scanAddressFeature);
        for (final AccessibilityFeature feature : features) {
            feature.onServiceConnected();
        }
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        Optional
                .ofNullable(event.getPackageName())
                .ifPresent(
                        packageName -> {
                            final String pkg = packageName.toString();

                            // WICHTIG: Wenn wir Google Maps verlassen, muessen Maps-Overlays sofort verschwinden.
                            // Das gilt auch, wenn wir zu Gemini oder in andere Systembereiche wechseln.
                            if (!GOOGLE_MAPS_PACKAGE.equals(pkg)) {
                                resetFeatures();
                            }

                            switch (pkg) {
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
                                        handleResolverEvent(event);
                                    }
                                    break;
                            }
                        });
    }

    private void handleGoogleAppEvent(final AccessibilityEvent event) {
        new AccessibilityServices(this)
                .getRootInActiveWindow()
                .ifPresent(root -> features.forEach(feature -> feature.onGoogleAppEvent(event, root)));
    }

    private void resetFeatures() {
        activeServiceHighlightFeature.hide();
        for (final AccessibilityFeature feature : features) {
            if (feature instanceof final AddStopFeature addStopFeature) {
                addStopFeature.reset();
            } else if (feature instanceof final SortFeature sortFeature) {
                sortFeature.reset();
            } else if (feature instanceof final ScanAddressFeature scanAddressFeatureInstance) {
                scanAddressFeatureInstance.reset();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        features.forEach(AccessibilityFeature::onDestroy);
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted.");
    }

    private void handleGoogleMapsEvent(final AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            resetFeatures();
        }
        urlRequester.handleGoogleMapsEvent(event);
        new AccessibilityServices(this)
                .getRootInActiveWindow()
                .ifPresent(
                        root -> {
                            stopCountDetector.detect(root);
                            features.forEach(feature -> feature.onGoogleMapsEvent(event, root));
                        });
    }

    private void handleResolverEvent(final AccessibilityEvent event) {
        urlRequester.handleResolverEvent();
    }

    private VehicleRoutingTransportCostsProvider getVehicleRoutingTransportCostsProvider() {
        if (SortConfig.getOptimizationMethod(this) == SortConfig.OptimizationMethod.HAVERSINE) {
            return new HaversineVehicleRoutingTransportCostsProvider();
        } else {
            return new OsrmVehicleRoutingTransportCostsProvider(
                    new OpenRouteServiceRoutingMatrixProvider(
                            ApiKeyRepository.getApiKey(this).orElseThrow()));
        }
    }
}
