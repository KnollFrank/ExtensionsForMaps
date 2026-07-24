package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContext;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContextResolver;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.StopCountDetector;
import de.KnollFrank.routeoptimizerforgooglemaps.common.AccessibilityServices;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.AccessibilityFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.AddStopFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.SortFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.HaversineVehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;

public class RouteOptimizerAccessibilityService extends AccessibilityService {

    private static final String TAG = RouteOptimizerAccessibilityService.class.getSimpleName();
    private static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";

    private List<AccessibilityFeature> features = List.of();
    private StopCountDetector stopCountDetector;
    private RouteUrlRequester urlRequester;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        urlRequester = new RouteUrlRequester(this);
        final GoogleMapsContext googleMapsContext = GoogleMapsContextResolver.resolve(this);
        final AddStopFeature addStopFeature =
                new AddStopFeature(
                        this,
                        googleMapsContext,
                        urlRequester,
                        routeUrl ->
                                DummyStopAdder.addDummyStopToDirectionsUrlThenOpenInGoogleMaps(
                                        routeUrl,
                                        this));
        final SortFeature sortFeature =
                new SortFeature(
                        this,
                        urlRequester,
                        routeUrl -> {
                            Log.d(TAG, "Extracted route URL for SORT: " + routeUrl);
                            final RouteOptimizationWorkflow routeOptimizationWorkflow =
                                    new RouteOptimizationWorkflow(
                                            new RouteOptimizer(new HaversineVehicleRoutingTransportCostsProvider()),
                                            RouteOptimizerAccessibilityService.this);
                            routeOptimizationWorkflow.optimizeThenShowRoute(routeUrl);
                        });
        stopCountDetector =
                new StopCountDetector(
                        googleMapsContext,
                        List.of(addStopFeature, sortFeature));
        features = List.of(addStopFeature, sortFeature);
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
                            switch (packageName.toString()) {
                                case GOOGLE_MAPS_PACKAGE -> handleGoogleMapsEvent(event);
                                case RESOLVER_PACKAGE -> handleResolverEvent(event);
                            }
                        });
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
}
