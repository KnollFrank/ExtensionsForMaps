package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.Optional;

class ShortDirectionsUrlFactory {

    public static Optional<ShortDirectionsUrl> createShortDirectionsUrl(final URL url) {
        return isShortDirectionsUrl(url) ?
                Optional.of(new ShortDirectionsUrl(url)) :
                Optional.empty();
    }

    private static boolean isShortDirectionsUrl(final URL url) {
        return isShortDirectionsUrlNewType(url) || isShortDirectionsUrlOldType(url);
    }

    private static boolean isShortDirectionsUrlNewType(final URL url) {
        return "maps.app.goo.gl".equals(url.getHost());
    }

    private static boolean isShortDirectionsUrlOldType(final URL url) {
        return "goo.gl".equals(url.getHost()) && url.getPath().startsWith("/maps");
    }
}
