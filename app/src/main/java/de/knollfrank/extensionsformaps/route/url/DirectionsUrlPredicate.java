package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;

public class DirectionsUrlPredicate {

    public static boolean isDirectionsUrl(final URL url) {
        return DirectionsUrlFactory
                .createDirectionsUrl(url)
                .isPresent();
    }

    public static boolean isLongDirectionsUrl(final URL url) {
        return DirectionsUrlFactory
                .createLongDirectionsUrl(url)
                .isPresent();
    }

    public static boolean isShortDirectionsUrl(final URL url) {
        return ShortDirectionsUrlFactory
                .createShortDirectionsUrl(url)
                .isPresent();
    }
}
