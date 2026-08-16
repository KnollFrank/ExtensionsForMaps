package de.knollfrank.extensionsformaps.route;

import java.net.URL;

public class DirectionsUrlPredicate {

    public static boolean isDirectionsUrl(final URL url) {
        return ModernDirectionsUrlFactory.isModernDirectionsUrl(url) || LegacyDirectionsUrlFactory.isLegacyDirectionsUrl(url);
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
