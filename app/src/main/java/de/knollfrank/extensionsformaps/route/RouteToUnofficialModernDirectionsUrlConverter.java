package de.knollfrank.extensionsformaps.route;

import android.net.Uri;

import java.util.List;
import java.util.stream.Collectors;

import de.knollfrank.extensionsformaps.common.URLs;
import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.route.protobuf.Datatype;
import de.knollfrank.extensionsformaps.route.protobuf.Node;
import de.knollfrank.extensionsformaps.route.protobuf.NodeFactory;
import de.knollfrank.extensionsformaps.route.protobuf.NodeSerializer;
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
        final int mapViewContext = 3;
        final int travelMode = 1;
        final int viewState = 4;
        final String drivingMode = "3";
        final String showDirectionsLayer = "1";
        return NodeFactory.createContainer(
                mapViewContext,
                List.of(
                        NodeFactory.createLeaf(travelMode, Datatype.ENUM, drivingMode),
                        NodeFactory.createLeaf(viewState, Datatype.BOOLEAN, showDirectionsLayer)));
    }

    private static Node createRouteDataNode(final List<Node> stopNodes) {
        return NodeFactory.createContainer(
                4,
                List.of(NodeFactory.createContainer(4, stopNodes)));
    }

    private static List<Node> createStopNodes(final List<Stop> stops) {
        return stops
                .stream()
                .map(RouteToUnofficialModernDirectionsUrlConverter::createStopNode)
                .toList();
    }

    private static Node createStopNode(final Stop stop) {
        final Angle lat = stop.geodetic().getLatitude();
        final Angle lon = stop.geodetic().getLongitude();

        return stop.officialPlaceId().isPresent() ?
                createStopNodeWithPlaceId(stop.officialPlaceId().get(), lat, lon) :
                createStopNodeWithoutPlaceId(lat, lon);
    }

    private static Node createStopNodeWithPlaceId(final OfficialPlaceId officialPlaceId,
                                                  final Angle lat,
                                                  final Angle lon) {
        // Linear sequence was: 1m5, 1m4, 1sPID, 8m2, 3dLAT, 4dLON
        // This corresponds to:
        // 1m5 -> [1m4 -> [1sPID, 8m2 -> [3dLAT, 4dLON]]]
        return NodeFactory.createContainer(
                1,
                List.of(
                        NodeFactory.createContainer(
                                1,
                                List.of(
                                        NodeFactory.createLeaf(1, Datatype.STRING, officialPlaceId.toUndocumentedPlaceId().value()),
                                        NodeFactory.createContainer(
                                                8,
                                                List.of(
                                                        NodeFactory.createLeaf(3, Datatype.DOUBLE, format(lat)),
                                                        NodeFactory.createLeaf(4, Datatype.DOUBLE, format(lon))))))));
    }

    private static Node createStopNodeWithoutPlaceId(final Angle lat, final Angle lon) {
        // Linear sequence was: 1m3, 2m2, 1dLON, 2dLAT
        // This corresponds to:
        // 1m3 -> [2m2 -> [1dLON, 2dLAT]]
        return NodeFactory.createContainer(
                1,
                List.of(
                        NodeFactory.createContainer(
                                2,
                                List.of(
                                        NodeFactory.createLeaf(1, Datatype.DOUBLE, format(lon)),
                                        NodeFactory.createLeaf(2, Datatype.DOUBLE, format(lat))))));
    }

    private static String format(final Angle angle) {
        return RouteToOfficialDirectionsUrlConverter.format(angle);
    }
}
