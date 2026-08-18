package de.knollfrank.extensionsformaps.route;

import java.net.URL;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Optionals;

public class RouteToUrlConverter {

    public static URL getUrl(final Route route) {
        return Optionals
                .streamOfPresentElements(
                        RouteToOfficialUrlConverter.getOfficialUrl(route),
                        Optional.of(RouteToUnofficialUrlConverter.getUnofficialUrl(route)))
                .findFirst()
                .orElseThrow();
    }
}