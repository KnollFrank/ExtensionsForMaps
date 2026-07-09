package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.Node;
import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.NodeFinder;
import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.NodesParser;

// FK-TODO: brauchen Gesamtunittest, der eine Google Maps Directions URL entgegennimmt und eine korrekt sortierte (optimierte) Google Maps Directions URL erzeugt.
public class GoogleMapsRouteExtractor {

    public static Route extractRouteFromDirectionsUrl(final URL directionsUrl) {
        return DirectionsUrl
                .of(directionsUrl)
                .map(GoogleMapsRouteExtractor::extractRoute)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid URL: %s is not a valid Google Maps directions URL.", directionsUrl)));
    }

    // FK-TODO: refactor
    private static Route extractRoute(final DirectionsUrl directionsUrl) {
        final List<StopData> stopDataList = AddressToStopDataConverter.convert(directionsUrl.getUrlDecodedAddresses());
        directionsUrl
                .getTokensFromDataPart()
                .map(NodesParser::parseAllNodes)
                .map(rootNodes -> NodeFinder.findWaypointContainers(rootNodes, stopDataList.size()))
                .ifPresent(
                        waypointContainers -> {
                            // 3. Extrahiere die Daten für jeden Stopp aus seinem jeweiligen Sub-Baum
                            // FK-TODO: use Lists.zip(stopDataList, waypointContainers);
                            for (int i = 0; i < waypointContainers.size(); i++) {
                                final Node waypointNode = waypointContainers.get(i);
                                final StopData stopData = stopDataList.get(i);
                                extractDataFromSubtree(waypointNode, stopData);
                            }
                        });
        return RouteFactory.createRoute(StopDataConverter.asStops(stopDataList));
    }

    // --- FLEXIBLE DATEN-EXTRAKTION AUS DEM SUB-BAUM ---
    // FK-TODO: refactor, das Parsen der Datentypen + Werte gehört in eine andere Klasse
    private static void extractDataFromSubtree(final Node node, final StopData stopData) {
        final Parser<String> placeIdParser = new PlaceIdParser();
        if (node.dataType == 's' && node.fieldId == 1) {
            if (placeIdParser.matches(node.token)) {
                stopData.placeId = Optional.of(placeIdParser.parse(node.token));
            }
        } else if (node.dataType == 'd') {
            final double value = Double.parseDouble(node.token.substring(String.valueOf(node.fieldId).length() + 1));
            // Flexibles Mapping für beide bekannten Google-Koordinatenformate (alt: 3d/4d, neu: 2d/1d)
            if (node.fieldId == 3 || node.fieldId == 2) {
                stopData.latitude = Optional.of(value);
            } else if (node.fieldId == 4 || node.fieldId == 1) {
                stopData.longitude = Optional.of(value);
            }
        }
        // Rekursiv tiefer suchen
        for (final Node child : node.children) {
            extractDataFromSubtree(child, stopData);
        }
    }
}