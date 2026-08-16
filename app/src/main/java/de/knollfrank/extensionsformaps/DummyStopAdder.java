package de.knollfrank.extensionsformaps;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import de.knollfrank.extensionsformaps.route.GoogleMapsRouteExtractor;
import de.knollfrank.extensionsformaps.route.RouteToUrlConverter;
import de.knollfrank.extensionsformaps.route.Routes;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrlPredicate;

public class DummyStopAdder {

    private static final String TAG = DummyStopAdder.class.getSimpleName();

    public static CompletableFuture<Void> addDummyStopToDirectionsUrlThenOpenInGoogleMaps(final URL directionsUrl,
                                                                                          final Context context) {
        return CompletableFuture
                .supplyAsync(() -> addDummyStop(directionsUrl))
                .handle((final URL directionsUrlWithDummyStop, final Throwable throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Error adding dummy stop to directions URL", throwable);
                        displayErrorProcessingRoute(context);
                    } else {
                        GoogleMapsNavigator.launchUrl(directionsUrlWithDummyStop, context);
                    }
                    return null;
                });
    }

    private static URL addDummyStop(final URL directionsUrl) {
        final URL expandedUrl = expandShortDirectionsUrl(directionsUrl);
        if (DirectionsUrlPredicate.isShortDirectionsUrl(expandedUrl)) {
            throw new RuntimeException("Could not expand short URL: " + expandedUrl);
        }
        return RouteToUrlConverter.getUrl(
                Routes.addDummyStop(
                        GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(expandedUrl)));
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
