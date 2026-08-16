package de.knollfrank.extensionsformaps.route;

import java.net.URL;
import java.util.List;

public class DirectionsUrlPredicate {

    public static boolean isDirectionsUrl(final URL url) {
        return isModernDirectionsUrl(url) || isLegacyDirectionsUrl(url);
    }

    public static boolean isModernDirectionsUrl(final URL url) {
        return List.of("http", "https").contains(url.getProtocol()) &&
                url.getHost().contains("google") &&
                url.getPath().startsWith("/maps/dir/");
    }

    public static boolean isLegacyDirectionsUrl(final URL url) {
        final String query = url.getQuery();
        final String path = url.getPath();
        return url.getHost().toLowerCase().contains("google") &&
                path.contains("/maps") &&
                query != null &&
                // FK-TODO: DRY with AddressesProvider's usage of saddr and daddr, extract constant
                (query.contains("saddr=") || query.contains("daddr="));
    }

    public static boolean isShortDirectionsUrl(final URL url) {
        return isShortDirectionsUrlNewType(url) || isShortDirectionsUrlOldType(url);
    }

    private static boolean isShortDirectionsUrlNewType(final URL url) {
        return "maps.app.goo.gl".equals(url.getHost());
    }

    private static boolean isShortDirectionsUrlOldType(final URL url) {
        return "goo.gl".equals(url.getHost()) && url.getPath().startsWith("/maps");
    }
}
