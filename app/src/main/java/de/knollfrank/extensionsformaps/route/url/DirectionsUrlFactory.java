package de.knollfrank.extensionsformaps.route.url;

import com.codepoetics.ambivalence.Either;

import java.net.URL;
import java.util.Optional;

public class DirectionsUrlFactory {

    public static Optional<Either<ModernDirectionsUrl, LegacyDirectionsUrl>> createDirectionsUrl(final URL url) {
        {
            final Optional<ModernDirectionsUrl> modernDirectionsUrl = ModernDirectionsUrlFactory.createModernDirectionsUrl(url);
            if (modernDirectionsUrl.isPresent()) {
                return Optional.of(Either.ofLeft(modernDirectionsUrl.get()));
            }
        }
        {
            final Optional<LegacyDirectionsUrl> legacyDirectionsUrl = LegacyDirectionsUrlFactory.createLegacyDirectionsUrl(url);
            if (legacyDirectionsUrl.isPresent()) {
                return Optional.of(Either.ofRight(legacyDirectionsUrl.get()));
            }
        }
        return Optional.empty();
    }
}
