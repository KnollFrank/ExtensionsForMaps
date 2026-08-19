package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Maps;
import de.knollfrank.extensionsformaps.common.URLs;

public class OfficialDirectionsUrlFactory {

    public static Optional<OfficialDirectionsUrl> createOfficialDirectionsUrl(final URL url) {
        return isOfficialDirectionsUrl(url) ?
                Optional.of(new OfficialDirectionsUrl(url)) :
                Optional.empty();
    }

    public static boolean isOfficialDirectionsUrl(final URL url) {
        return url.getPath().startsWith("/maps/dir") &&
                Maps
                        .get(URLs.getQuery(url), "api")
                        .filter("1"::equals)
                        .isPresent();
    }
}
