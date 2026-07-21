package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import de.KnollFrank.routeoptimizerforgooglemaps.route.DirectionsUrlPredicate;
import de.KnollFrank.routeoptimizerforgooglemaps.route.GoogleMapsRouteExtractor;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Routes;

class DummyStopAdder {

    public static void addDummyStopToDirectionsUrlThenOpenInGoogleMaps(final URL directionsUrl, final Context context) {
        CompletableFuture
                .supplyAsync(() -> addDummyStop(directionsUrl))
                .whenComplete((final URL directionsUrlWithDummyStop, final Throwable throwable) -> {
                    if (throwable != null) {
                        displayErrorProcessingRoute(context);
                        return;
                    }
                    GoogleMapsNavigator.launchUrl(directionsUrlWithDummyStop, context);
                });
    }

    private static URL addDummyStop(final URL directionsUrl) {
        return RouteToUrlConverter.getUrl(
                Routes.addDummyStop(
                        GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(
                                expandShortDirectionsUrl(directionsUrl))));
    }

    private static URL expandShortDirectionsUrl(final URL directionsUrl) {
        try {
            return DirectionsUrlPredicate.isShortDirectionsUrl(directionsUrl) ?
                    UrlExpander.expandUrl(directionsUrl) :
                    directionsUrl;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void displayErrorProcessingRoute(final Context context) {
        runOnUiThread(
                () -> Toast
                        .makeText(context, R.string.error_processing_route, Toast.LENGTH_LONG)
                        .show());
    }

    private static void runOnUiThread(final Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
