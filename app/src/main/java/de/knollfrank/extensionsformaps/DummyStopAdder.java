package de.knollfrank.extensionsformaps;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import de.knollfrank.extensionsformaps.route.GoogleMapsRouteExtractor;
import de.knollfrank.extensionsformaps.route.RouteToUrlConverter;
import de.knollfrank.extensionsformaps.route.Routes;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class DummyStopAdder {

    private static final String TAG = DummyStopAdder.class.getSimpleName();

    public static CompletableFuture<Void> addDummyStopToDirectionsUrlThenOpenInGoogleMaps(final DirectionsUrl directionsUrl,
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

    private static URL addDummyStop(final DirectionsUrl directionsUrl) {
        return RouteToUrlConverter.getUrl(
                Routes.addDummyStop(
                        GoogleMapsRouteExtractor.extractRoute(
                                directionsUrl)));
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
