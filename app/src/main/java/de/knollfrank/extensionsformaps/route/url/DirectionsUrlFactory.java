package de.knollfrank.extensionsformaps.route.url;

import com.codepoetics.ambivalence.Either;

import java.net.URL;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Optionals;

public class DirectionsUrlFactory {

    public static Optional<Either<ModernDirectionsUrl, LegacyDirectionsUrl>> createDirectionsUrl(final URL url) {
        return Optionals
                .streamOfPresentElements(
                        ModernDirectionsUrlFactory
                                .createModernDirectionsUrl(url)
                                .<Either<ModernDirectionsUrl, LegacyDirectionsUrl>>map(Either::ofLeft),
                        LegacyDirectionsUrlFactory
                                .createLegacyDirectionsUrl(url)
                                .<Either<ModernDirectionsUrl, LegacyDirectionsUrl>>map(Either::ofRight))
                .findFirst();
    }
}
