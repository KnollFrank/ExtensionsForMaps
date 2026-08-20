package de.knollfrank.extensionsformaps.route;

import de.knollfrank.extensionsformaps.route.extract.GoogleMapsRouteExtractor;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class RouteDirectionsUrlConverter {

    public static DirectionsUrl getDirectionsUrl(final Route route) {
        return RouteToDirectionsUrlConverter.getDirectionsUrl(route);
    }

    public static Route getRoute(final DirectionsUrl directionsUrl) {
        return GoogleMapsRouteExtractor.extractRoute(directionsUrl);
    }
}
