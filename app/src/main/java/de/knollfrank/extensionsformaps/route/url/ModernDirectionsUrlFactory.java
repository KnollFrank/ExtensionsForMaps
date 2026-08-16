package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;
import java.util.Optional;

class ModernDirectionsUrlFactory {

    public static Optional<ModernDirectionsUrl> createModernDirectionsUrl(final URL url) {
        return isModernDirectionsUrl(url) ?
                Optional.of(new ModernDirectionsUrl(url)) :
                Optional.empty();
    }

    public static boolean isModernDirectionsUrl(final URL url) {
        return List.of("http", "https").contains(url.getProtocol()) &&
                url.getHost().contains("google") &&
                url.getPath().startsWith("/maps/dir/");
    }
}
