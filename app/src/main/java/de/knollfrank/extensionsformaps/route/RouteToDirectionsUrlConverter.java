package de.knollfrank.extensionsformaps.route;

import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Optionals;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class RouteToDirectionsUrlConverter {

    public static DirectionsUrl getDirectionsUrl(final Route route) {
        return Optionals
                .streamOfPresentElements(
                        RouteToOfficialDirectionsUrlConverter.getOfficialDirectionsUrl(route),
                        Optional.of(RouteToUnofficialModernDirectionsUrlConverter.getUnofficialModernDirectionsUrl(route)))
                .findFirst()
                .orElseThrow();
    }
}