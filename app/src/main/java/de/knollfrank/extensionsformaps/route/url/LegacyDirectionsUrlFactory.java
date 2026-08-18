package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.Optional;

// FK-TODO: move createLegacyDirectionsUrl() into LegacyDirectionsUrl and check in it's constructor. Dito for ModernDirectionsUrlFactory
class LegacyDirectionsUrlFactory {

    public static Optional<LegacyDirectionsUrl> createLegacyDirectionsUrl(final URL url) {
        return isLegacyDirectionsUrl(url) ?
                Optional.of(new LegacyDirectionsUrl(url)) :
                Optional.empty();
    }

    private static boolean isLegacyDirectionsUrl(final URL url) {
        final String query = url.getQuery();
        final String path = url.getPath();
        return url.getHost().toLowerCase().contains("google") &&
                path.contains("/maps") &&
                query != null &&
                // FK-TODO: DRY with AddressesProvider's usage of saddr and daddr, extract constant
                (query.contains("saddr=") || query.contains("daddr="));
    }
}
