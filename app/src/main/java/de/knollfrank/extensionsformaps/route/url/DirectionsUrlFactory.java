package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import de.knollfrank.extensionsformaps.common.Optionals;

public class DirectionsUrlFactory {

    public static CompletableFuture<Optional<DirectionsUrl>> createDirectionsUrl(final URL url) {
        return Optionals
                .streamOfPresentElements(
                        DirectionsUrlFactory
                                ._createDirectionsUrl(url)
                                .map(CompletableFuture::completedFuture),
                        ShortDirectionsUrlFactory
                                .createShortDirectionsUrl(url)
                                .map(ShortDirectionsUrl::expand))
                .findFirst()
                .map(directionsUrlCompletableFuture -> directionsUrlCompletableFuture.thenApply(Optional::of))
                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    private static Optional<DirectionsUrl> _createDirectionsUrl(final URL url) {
        return Optionals
                .<DirectionsUrl>streamOfPresentElements(
                        ModernDirectionsUrlFactory.createModernDirectionsUrl(url),
                        LegacyDirectionsUrlFactory.createLegacyDirectionsUrl(url))
                .findFirst();
    }
}
