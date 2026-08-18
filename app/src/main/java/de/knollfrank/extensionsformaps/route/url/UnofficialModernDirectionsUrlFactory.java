package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class UnofficialModernDirectionsUrlFactory {

    public static Optional<UnofficialModernDirectionsUrl> createUnofficialModernDirectionsUrl(final URL url) {
        return isUnofficialModernDirectionsUrl(url) ?
                Optional.of(new UnofficialModernDirectionsUrl(url)) :
                Optional.empty();
    }

    private static boolean isUnofficialModernDirectionsUrl(final URL url) {
        return List.of("http", "https").contains(url.getProtocol()) &&
                url.getHost().contains("google") &&
                url.getPath().startsWith("/maps/dir/");
    }
}
