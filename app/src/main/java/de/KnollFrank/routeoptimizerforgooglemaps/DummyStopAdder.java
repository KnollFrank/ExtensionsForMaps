package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;

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
                .thenAccept(directionsUrlWithDummyStop -> GoogleMapsNavigator.launchUrl(directionsUrlWithDummyStop, context));
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
}
