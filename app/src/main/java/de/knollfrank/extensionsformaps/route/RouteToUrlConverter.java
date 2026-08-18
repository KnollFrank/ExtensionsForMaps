package de.knollfrank.extensionsformaps.route;

import java.net.URL;

public class RouteToUrlConverter {

    public static URL getUrl(final Route route) {
        return route.stops().size() <= 10 ?
                RouteToOfficialUrlConverter.getOfficialUrl(route) :
                RouteToUnofficialUrlConverter.getUnofficialUrl(route);
    }
}