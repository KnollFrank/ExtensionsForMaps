package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContext;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContextResolver;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.StopCountDetector;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.AccessibilityFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.AddStopFeature;
import de.KnollFrank.routeoptimizerforgooglemaps.feature.SortFeature;

public class RouteOptimizerAccessibilityService extends AccessibilityService {

    private static final String TAG = RouteOptimizerAccessibilityService.class.getSimpleName();
    private static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";

    private final List<AccessibilityFeature> features = new ArrayList<>();
    private StopCountDetector stopCountDetector;
    private RouteUrlRequester urlRequester;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        final GoogleMapsContext googleMapsContext = GoogleMapsContextResolver.resolve(this);

        urlRequester = new RouteUrlRequester(this);
        stopCountDetector = new StopCountDetector(googleMapsContext);

        final AddStopFeature addStopFeature = new AddStopFeature(this, googleMapsContext, urlRequester);
        final SortFeature sortFeature = new SortFeature(this, urlRequester);

        stopCountDetector.addListener(addStopFeature);
        stopCountDetector.addListener(sortFeature);

        features.add(addStopFeature);
        features.add(sortFeature);

        for (final AccessibilityFeature feature : features) {
            feature.onServiceConnected();
        }
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        final CharSequence packageName = event.getPackageName();
        if (packageName == null) {
            return;
        }

        switch (packageName.toString()) {
            case GOOGLE_MAPS_PACKAGE -> handleGoogleMapsEvent(event);
            case RESOLVER_PACKAGE -> urlRequester.handleResolverEvent();
        }
    }

    private void handleGoogleMapsEvent(final AccessibilityEvent event) {
        urlRequester.handleGoogleMapsEvent(event);

        final AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }

        stopCountDetector.detect(root);

        for (final AccessibilityFeature feature : features) {
            feature.onGoogleMapsEvent(event, root);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (final AccessibilityFeature feature : features) {
            feature.onDestroy();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted.");
    }
}
