package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContext;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContextResolver;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.StopCountDetector;
import de.KnollFrank.routeoptimizerforgooglemaps.common.AccessibilityServices;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.AccessibilityFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.ActiveServiceHighlightFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.AddStopFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.ScanAddressFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.SortFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.HaversineVehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.OsrmVehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.VehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

public class RouteOptimizerAccessibilityService extends AccessibilityService {

    private static final String TAG = RouteOptimizerAccessibilityService.class.getSimpleName();
    private static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";
    private static final String SYSTEM_PACKAGE = "android";
    private static final String GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox";

    private List<AccessibilityFeature> features = List.of();
    private StopCountDetector stopCountDetector;
    private RouteUrlRequester urlRequester;
    private ActiveServiceHighlightFeature activeServiceHighlightFeature;
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
                                final ProgressOverlay progressOverlay = new ProgressOverlay(RouteOptimizerAccessibilityService.this);
                                progressOverlay.show();
                                DummyStopAdder
                                        .addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
                                                routeUrl,
                                                RouteOptimizerAccessibilityService.this)
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
                                            RouteOptimizerAccessibilityService.this);
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
                            switch (pkg) {
                                case GOOGLE_MAPS_PACKAGE -> handleGoogleMapsEvent(event);
                                case RESOLVER_PACKAGE -> handleResolverEvent(event);
                                case SYSTEM_PACKAGE -> {
                                    if (urlRequester.isWaitingForUrl()) {
                                        handleResolverEvent(event);
                                    }
                                }
                                case GOOGLE_APP_PACKAGE -> handleGoogleAppEvent(event);
                            }
                        });
    }

    private void handleGoogleAppEvent(final AccessibilityEvent event) {
        // Placeholder for future Lens extraction logic
        Log.d(TAG, "Received event from Google App (Lens): " + event.getEventType());
    }

    // FK-TODO: refactor using streams
    private void resetFeatures() {
        activeServiceHighlightFeature.hide();
        for (final AccessibilityFeature feature : features) {
            if (feature instanceof final AddStopFeature addStopFeature) {
                addStopFeature.reset();
            } else if (feature instanceof final SortFeature sortFeature) {
                sortFeature.reset();
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
        AccessibilityServices
                .getRootInActiveWindow(this)
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
