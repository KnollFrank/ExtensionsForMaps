package de.knollfrank.extensionsformaps.route.extract;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Lists;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteFactory;
import de.knollfrank.extensionsformaps.route.protobuf.Datatype;
import de.knollfrank.extensionsformaps.route.protobuf.Node;
import de.knollfrank.extensionsformaps.route.protobuf.NodeFinder;
import de.knollfrank.extensionsformaps.route.protobuf.NodesParser;
import de.knollfrank.extensionsformaps.route.url.UnofficialModernDirectionsUrl;

class ModernDirectionsUrlRouteExtractor {

    // FK-TODO: refactor
    public static Route extractRoute(final UnofficialModernDirectionsUrl directionsUrl) {
        final List<StopData> stopDataList = AddressToStopDataConverter.convert(directionsUrl.getUrlDecodedAddresses());
        // 3. Extrahiere die Daten für jeden Stopp aus seinem jeweiligen Sub-Baum
        NodeFinder
                .findWaypointContainers(
                        NodesParser.parseNodes(directionsUrl.getTokensFromDataPart()),
                        stopDataList.size())
                .ifPresent(
                        waypointContainers ->
                                Lists
                                        .zip(waypointContainers, stopDataList)
                                        .forEach(waypoint_stopData -> extractDataFromSubtreeIntoStopData(waypoint_stopData.first, waypoint_stopData.second)));
        return RouteFactory.createRoute(StopDataConverter.asStops(stopDataList));
    }

    // --- FLEXIBLE DATEN-EXTRAKTION AUS DEM SUB-BAUM ---
    // FK-TODO: refactor, das Parsen der Datentypen + Werte gehört in eine andere Klasse
    private static void extractDataFromSubtreeIntoStopData(final Node node, final StopData stopData) {
        if (PlaceIdParser.isPlaceIdNode(node)) {
            stopData.officialPlaceId = Optional.of(PlaceIdParser.getOfficialPlaceId(node));
        } else if (Datatype.DOUBLE.equals(node.datatype())) {
            final double value = Double.parseDouble(node.value());
            if (node.fieldId() == 3 || node.fieldId() == 2) {
                stopData.latitude = Optional.of(value);
            } else if (node.fieldId() == 4 || node.fieldId() == 1) {
                stopData.longitude = Optional.of(value);
            }
        }
        // Rekursiv tiefer suchen
        for (final Node child : node.children()) {
            extractDataFromSubtreeIntoStopData(child, stopData);
        }
    }
}
