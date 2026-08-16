package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Optionals;

public class DirectionsUrlFactory {

    public static Optional<DirectionsUrl> createDirectionsUrl(final URL url) {
        return Optionals
                .streamOfPresentElements(
                        ModernDirectionsUrlFactory.createModernDirectionsUrl(url),
                        LegacyDirectionsUrlFactory.createLegacyDirectionsUrl(url),
                        ShortDirectionsUrlFactory
                                .createShortDirectionsUrl(url)
                                .map(ShortDirectionsUrl::expand))
                .findFirst();
    }
}
