package de.knollfrank.extensionsformaps.route;

import android.net.Uri;

import java.util.List;

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
        final StringBuilder pathBuilder = new StringBuilder("https://www.google.com/maps/dir");
        for (final Stop stop : route.stops()) {
            pathBuilder.append("/").append(Uri.encode(stop.address()));
        }

        final List<Node> rootNodes =
                List.of(
                        NodeFactory.createContainer(
                                3,
                                List.of(
                                        NodeFactory.createLeaf(1, Datatype.ENUM, "3"),
                                        NodeFactory.createLeaf(4, Datatype.BOOLEAN, "1"))),
                        NodeFactory.createContainer(
                                4,
                                List.of(NodeFactory.createContainer(4, createStopNodes(route.stops())))));

        pathBuilder.append("/data=").append(NodeSerializer.serialize(rootNodes)).append("?entry=ttu");
        return UnofficialModernDirectionsUrlFactory
                .createUnofficialModernDirectionsUrl(URLs.createUrl(pathBuilder.toString()))
                .orElseThrow();
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
