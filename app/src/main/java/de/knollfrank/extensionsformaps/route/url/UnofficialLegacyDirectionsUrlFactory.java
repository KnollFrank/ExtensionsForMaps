package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.Optional;

class UnofficialLegacyDirectionsUrlFactory {

    public static Optional<UnofficialLegacyDirectionsUrl> createUnofficialLegacyDirectionsUrl(final URL url) {
        return isUnofficialLegacyDirectionsUrl(url) ?
                Optional.of(new UnofficialLegacyDirectionsUrl(url)) :
                Optional.empty();
    }

    private static boolean isUnofficialLegacyDirectionsUrl(final URL url) {
        final String query = url.getQuery();
        final String path = url.getPath();
        return url.getHost().toLowerCase().contains("google") &&
                path.contains("/maps") &&
                query != null &&
                // FK-TODO: DRY with AddressesProvider's usage of saddr and daddr, extract constant
                (query.contains("saddr=") || query.contains("daddr="));
    }
}
