package de.knollfrank.extensionsformaps.route.extract;

import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.UnofficialLegacyDirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.UnofficialModernDirectionsUrl;

// FK-TODO: brauchen Gesamtunittest, der eine Google Maps Directions URL entgegennimmt und eine korrekt sortierte (optimierte) Google Maps Directions URL erzeugt.
// FK-TODO: GoogleMapsRouteExtractor und RouteToUrlConverter sind invers zueinander. Führe wie in SettingsSearch ein Converter-Interface ein
public class GoogleMapsRouteExtractor {

    public static Route extractRoute(final DirectionsUrl directionsUrl) {
        return switch (directionsUrl) {
            case UnofficialModernDirectionsUrl unofficialModernDirectionsUrl -> UnofficialModernDirectionsUrlRouteExtractor.extractRoute(unofficialModernDirectionsUrl);
            case UnofficialLegacyDirectionsUrl unofficialLegacyDirectionsUrl -> UnofficialLegacyDirectionsUrlRouteExtractor.extractRoute(unofficialLegacyDirectionsUrl);
        };
    }
}