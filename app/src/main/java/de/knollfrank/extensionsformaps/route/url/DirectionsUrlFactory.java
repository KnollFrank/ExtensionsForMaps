package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Optionals;

public class DirectionsUrlFactory {

    public static Optional<DirectionsUrl> createDirectionsUrl(final URL url) {
        return Optionals
                .streamOfPresentElements(
                        createLongDirectionsUrl(url),
                        ShortDirectionsUrlFactory.createShortDirectionsUrl(url))
                .findFirst();
    }

    public static Optional<? extends LongDirectionsUrl> createLongDirectionsUrl(final URL url) {
        return Optionals
                .streamOfPresentElements(
                        ModernDirectionsUrlFactory.createModernDirectionsUrl(url),
                        LegacyDirectionsUrlFactory.createLegacyDirectionsUrl(url))
                .findFirst();
    }
}
