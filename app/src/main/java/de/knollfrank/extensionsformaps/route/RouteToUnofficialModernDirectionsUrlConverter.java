package de.knollfrank.extensionsformaps.route;

import android.net.Uri;

import java.util.List;
import java.util.stream.Collectors;

import de.knollfrank.extensionsformaps.common.URLs;
import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.route.protobuf.Datatype;
import de.knollfrank.extensionsformaps.route.protobuf.DirectionsVisibility;
import de.knollfrank.extensionsformaps.route.protobuf.Node;
import de.knollfrank.extensionsformaps.route.protobuf.NodeFactory;
import de.knollfrank.extensionsformaps.route.protobuf.NodeSerializer;
import de.knollfrank.extensionsformaps.route.protobuf.TravelMode;
import de.knollfrank.extensionsformaps.route.url.UnofficialModernDirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.UnofficialModernDirectionsUrlFactory;

class RouteToUnofficialModernDirectionsUrlConverter {

    public static UnofficialModernDirectionsUrl getUnofficialModernDirectionsUrl(final Route route) {
        final String url = getUrl(route);
        return UnofficialModernDirectionsUrlFactory
                .createUnofficialModernDirectionsUrl(URLs.createUrl(url))
                .orElseThrow();
    }

    private static String getUrl(final Route route) {
        return "https://www.google.com/maps/dir" +
                "/" + getAddresses(route.stops()) +
                "/data=" + getDataPart(route.stops()) +
                "?entry=ttu";
    }

    private static String getAddresses(final List<Stop> stops) {
        return stops
                .stream()
                .map(RouteToUnofficialModernDirectionsUrlConverter::getAddress)
                .collect(Collectors.joining("/"));
    }

    private static String getAddress(final Stop stop) {
        return Uri.encode(stop.address());
    }

    private static String getDataPart(final List<Stop> stops) {
        return NodeSerializer.serialize(
                List.of(
                        createMapViewContextNode(),
                        createRouteDataNode(createStopNodes(stops))));
    }

    private static Node createMapViewContextNode() {
        final int mapViewContextId = 3;
        return NodeFactory.createContainer(
                mapViewContextId,
                List.of(
                        createTravelModeNode(TravelMode.DRIVING),
                        createViewStateNode(DirectionsVisibility.VISIBLE)));
    }

    private static Node createTravelModeNode(final TravelMode travelMode) {
        final int travelModeId = 1;
        return NodeFactory.createLeaf(travelModeId, Datatype.ENUM, travelMode.value);
    }

    private static Node createViewStateNode(final DirectionsVisibility directionsVisibility) {
        final int viewStateId = 4;
        return NodeFactory.createLeaf(viewStateId, Datatype.BOOLEAN, directionsVisibility.value);
    }

    private static Node createRouteDataNode(final List<Node> stopNodes) {
        final int routeDataId = 4;
        return NodeFactory.createContainer(
                routeDataId,
                List.of(createStopsNode(stopNodes)));
    }

    private static Node createStopsNode(final List<Node> stopNodes) {
        final int stopsId = 4;
        return NodeFactory.createContainer(stopsId, stopNodes);
    }

    private static List<Node> createStopNodes(final List<Stop> stops) {
        return stops
                .stream()
                .map(RouteToUnofficialModernDirectionsUrlConverter::createStopNode)
                .toList();
    }

    private static Node createStopNode(final Stop stop) {
        return stop
                .officialPlaceId()
                .map(officialPlaceId -> createStopNodeWithPlaceId(stop.geodetic(), officialPlaceId))
                .orElseGet(() -> createStopNodeWithoutPlaceId(stop.geodetic()));
    }

    private static Node createStopNodeWithPlaceId(final Geodetic geodetic, final OfficialPlaceId officialPlaceId) {
        final int stopEntryId = 1;
        final int stopDetailsId = 1;
        final int placeIdFieldId = 1;
        final int geodeticCoordinatesId = 8;
        final int latitudeFieldId = 3;
        final int longitudeFieldId = 4;
        // Linear sequence was: 1m5, 1m4, 1sPID, 8m2, 3dLAT, 4dLON
        // This corresponds to:
        // 1m5 -> [1m4 -> [1sPID, 8m2 -> [3dLAT, 4dLON]]]
        return NodeFactory.createContainer(
                stopEntryId,
                List.of(
                        NodeFactory.createContainer(
                                stopDetailsId,
                                List.of(
                                        NodeFactory.createLeaf(
                                                placeIdFieldId,
                                                Datatype.STRING,
                                                officialPlaceId.toUndocumentedPlaceId().value()),
                                        NodeFactory.createContainer(
                                                geodeticCoordinatesId,
                                                List.of(
                                                        NodeFactory.createLeaf(
                                                                latitudeFieldId,
                                                                Datatype.DOUBLE,
                                                                format(geodetic.getLatitude())),
                                                        NodeFactory.createLeaf(
                                                                longitudeFieldId,
                                                                Datatype.DOUBLE,
                                                                format(geodetic.getLongitude()))))))));
    }

    private static Node createStopNodeWithoutPlaceId(final Geodetic geodetic) {
        final int stopEntryId = 1;
        final int coordinatesId = 2;
        final int longitudeFieldId = 1;
        final int latitudeFieldId = 2;
        // Linear sequence was: 1m3, 2m2, 1dLON, 2dLAT
        // This corresponds to:
        // 1m3 -> [2m2 -> [1dLON, 2dLAT]]
        return NodeFactory.createContainer(
                stopEntryId,
                List.of(
                        NodeFactory.createContainer(
                                coordinatesId,
                                List.of(
                                        NodeFactory.createLeaf(
                                                longitudeFieldId,
                                                Datatype.DOUBLE,
                                                format(geodetic.getLongitude())),
                                        NodeFactory.createLeaf(
                                                latitudeFieldId,
                                                Datatype.DOUBLE,
                                                format(geodetic.getLatitude()))))));
    }

    private static String format(final Angle angle) {
        return RouteToOfficialDirectionsUrlConverter.format(angle);
    }
}
