package de.knollfrank.extensionsformaps.route.url;

import com.codepoetics.ambivalence.Either;

import java.net.URL;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Optionals;

public class DirectionsUrlFactory {

    public static Optional<Either<ModernDirectionsUrl, LegacyDirectionsUrl>> createDirectionsUrl(final URL url) {
        return Optionals
                .streamOfPresentElements(
                        createModernDirectionsUrl(url),
                        createLegacyDirectionsUrl(url))
                .findFirst();
    }

    private static Optional<Either<ModernDirectionsUrl, LegacyDirectionsUrl>> createModernDirectionsUrl(final URL url) {
        return ModernDirectionsUrlFactory
                .createModernDirectionsUrl(url)
                .map(Either::ofLeft);
    }

    private static Optional<Either<ModernDirectionsUrl, LegacyDirectionsUrl>> createLegacyDirectionsUrl(final URL url) {
        return LegacyDirectionsUrlFactory
                .createLegacyDirectionsUrl(url)
                .map(Either::ofRight);
    }
}
