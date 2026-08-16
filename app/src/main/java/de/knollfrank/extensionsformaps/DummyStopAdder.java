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
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrlFactory;
import de.knollfrank.extensionsformaps.route.url.LongDirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.ShortDirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.UrlExpander;

public class DummyStopAdder {

    private static final String TAG = DummyStopAdder.class.getSimpleName();

    // FK-TODO: replace URL with DirectionsUrl or LongDirectionsUrl
    public static CompletableFuture<Void> addDummyStopToDirectionsUrlThenOpenInGoogleMaps(final URL url,
                                                                                          final Context context) {
        return CompletableFuture
                .supplyAsync(() -> addDummyStop(url))
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

    private static URL addDummyStop(final URL url) {
        final DirectionsUrl directionsUrl =
                DirectionsUrlFactory
                        .createDirectionsUrl(url)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid URL: " + url));
        try {
            final LongDirectionsUrl longDirectionsUrl =
                    directionsUrl instanceof final ShortDirectionsUrl shortUrl ?
                            UrlExpander.expandUrl(shortUrl) :
                            (LongDirectionsUrl) directionsUrl;

            return RouteToUrlConverter.getUrl(
                    Routes.addDummyStop(
                            GoogleMapsRouteExtractor.extractRoute(longDirectionsUrl)));
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
