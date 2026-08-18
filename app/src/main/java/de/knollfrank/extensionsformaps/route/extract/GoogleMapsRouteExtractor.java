package de.knollfrank.extensionsformaps.route.extract;

import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.LegacyDirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.ModernDirectionsUrl;

// FK-TODO: brauchen Gesamtunittest, der eine Google Maps Directions URL entgegennimmt und eine korrekt sortierte (optimierte) Google Maps Directions URL erzeugt.
// FK-TODO: GoogleMapsRouteExtractor und RouteToUrlConverter sind invers zueinander. Führe wie in SettingsSearch ein Converter-Interface ein
public class GoogleMapsRouteExtractor {

    public static Route extractRoute(final DirectionsUrl directionsUrl) {
        if (directionsUrl instanceof final ModernDirectionsUrl modernDirectionsUrl) {
            return ModernDirectionsUrlRouteExtractor.extractRoute(modernDirectionsUrl);
        } else if (directionsUrl instanceof final LegacyDirectionsUrl legacyDirectionsUrl) {
            return LegacyDirectionsUrlRouteExtractor.extractRoute(legacyDirectionsUrl);
        }
        throw new IllegalArgumentException("Unsupported DirectionsUrl type: " + directionsUrl.getClass().getName());
    }
}