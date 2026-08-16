package de.knollfrank.extensionsformaps.route;

import com.codepoetics.ambivalence.Either;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.route.protobuf.Datatype;
import de.knollfrank.extensionsformaps.route.protobuf.Node;
import de.knollfrank.extensionsformaps.route.protobuf.NodeFinder;
import de.knollfrank.extensionsformaps.route.protobuf.NodesParser;

// FK-TODO: brauchen Gesamtunittest, der eine Google Maps Directions URL entgegennimmt und eine korrekt sortierte (optimierte) Google Maps Directions URL erzeugt.
// FK-TODO: GoogleMapsRouteExtractor und RouteToUrlConverter sind invers zueinander. Führe wie in SettingsSearch ein Converter-Interface ein
public class GoogleMapsRouteExtractor {

    public static Route extractRouteFromDirectionsUrl(final URL directionsUrl) {
        return DirectionsUrlFactory
                .createDirectionsUrl(directionsUrl)
                .map(GoogleMapsRouteExtractor::extractRoute)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid URL: %s is not a valid Google Maps directions URL.", directionsUrl)));
    }

    private static Route extractRoute(final Either<ModernDirectionsUrl, LegacyDirectionsUrl> directionsUrl) {
        return directionsUrl.join(
                GoogleMapsRouteExtractor::extractRoute,
                GoogleMapsRouteExtractor::extractRoute);
    }

    // FK-TODO: refactor
    private static Route extractRoute(final LegacyDirectionsUrl directionsUrl) {
        final List<StopData> stopDataList = AddressToStopDataConverter.convert(directionsUrl.getUrlDecodedAddresses());
        final List<String> tokens = directionsUrl.getGeocodeTokens();
        for (int i = 0; i < Math.min(tokens.size(), stopDataList.size()); i++) {
            final String token = tokens.get(i);
            final StopData stopData = stopDataList.get(i);
            GeocodeTokenParser
                    .parseToken(token)
                    .ifPresent(data -> {
                        data.latitude.ifPresent(lat -> stopData.latitude = Optional.of(lat));
                        data.longitude.ifPresent(lon -> stopData.longitude = Optional.of(lon));
                        data.featureId.ifPresent(fid -> stopData.officialPlaceId = Optional.of(new OfficialPlaceId(fid)));
                    });
        }
        return RouteFactory.createRoute(StopDataConverter.asStops(stopDataList));
    }

    // FK-TODO: refactor
    private static Route extractRoute(final ModernDirectionsUrl directionsUrl) {
        final List<StopData> stopDataList = AddressToStopDataConverter.convert(directionsUrl.getUrlDecodedAddresses());
        final List<Node> waypointContainers =
                NodeFinder.findWaypointContainers(
                        NodesParser.parseNodes(directionsUrl.getTokensFromDataPart()),
                        stopDataList.size());
        // 3. Extrahiere die Daten für jeden Stopp aus seinem jeweiligen Sub-Baum
        for (int i = 0; i < waypointContainers.size(); i++) {
            final Node waypoint = waypointContainers.get(i);
            final StopData stopData = stopDataList.get(i);
            extractDataFromSubtree(waypoint, stopData);
        }
        return RouteFactory.createRoute(StopDataConverter.asStops(stopDataList));
    }

    // --- FLEXIBLE DATEN-EXTRAKTION AUS DEM SUB-BAUM ---
    // FK-TODO: refactor, das Parsen der Datentypen + Werte gehört in eine andere Klasse
    private static void extractDataFromSubtree(final Node node, final StopData stopData) {
        if (PlaceIdParser.isPlaceIdNode(node)) {
            stopData.officialPlaceId = Optional.of(PlaceIdParser.getOfficialPlaceId(node));
        } else if (Datatype.DOUBLE.equals(node.datatype())) {
            final double value = Double.parseDouble(node.value());
            // Flexibles Mapping für beide bekannten Google-Koordinatenformate (alt: 3d/4d, neu: 2d/1d)
            if (node.fieldId() == 3 || node.fieldId() == 2) {
                stopData.latitude = Optional.of(value);
            } else if (node.fieldId() == 4 || node.fieldId() == 1) {
                stopData.longitude = Optional.of(value);
            }
        }
        // Rekursiv tiefer suchen
        for (final Node child : node.children()) {
            extractDataFromSubtree(child, stopData);
        }
    }
}