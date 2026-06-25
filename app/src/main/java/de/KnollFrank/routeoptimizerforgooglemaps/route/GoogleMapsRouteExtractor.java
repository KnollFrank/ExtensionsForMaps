package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
        final List<String> segments = directionsUrl.getSegments();
        if (segments.isEmpty()) {
            return new Route(List.of());
        }
        final List<StopData> stopDataList = SegmentToStopDataFromConverter.convert(segments);
        directionsUrl
                .getTokensFromDataPart()
                .map(List::iterator)
                .map(GoogleMapsRouteExtractor::parseAllNodes)
                .map(rootNodes -> findWaypointContainers(rootNodes, stopDataList.size()))
                .ifPresent(
                        waypointContainers -> {
                            // 3. Extrahiere die Daten für jeden Stopp aus seinem jeweiligen Sub-Baum
                            for (int i = 0; i < waypointContainers.size(); i++) {
                                final Node waypointNode = waypointContainers.get(i);
                                final StopData stopData = stopDataList.get(i);
                                extractDataFromSubtree(waypointNode, stopData);
                            }
                        });
        return new Route(StopDataConverter.asStops(stopDataList));
    }

    // --- REKURSIVER BAUM-PARSER (Tree Builder) ---
    // FK-TODO: refactor
    private static List<Node> parseAllNodes(final Iterator<String> stream) {
        final List<Node> nodes = new ArrayList<>();
        while (stream.hasNext()) {
            final String token = stream.next();
            final Node node = new Node(token);
            if (node.isContainer()) {
                final int subCount = node.getContainerSize();
                final List<Node> subNodes = parseNodes(stream, subCount);
                node.children.addAll(subNodes);
            }
            nodes.add(node);
        }
        return nodes;
    }

    // FK-TODO: refactor
    private static List<Node> parseNodes(final Iterator<String> stream, final int toConsume) {
        final List<Node> nodes = new ArrayList<>();
        int consumed = 0;

        while (consumed < toConsume && stream.hasNext()) {
            final String token = stream.next();
            consumed++;

            final Node node = new Node(token);
            if (node.isContainer()) {
                final int subCount = node.getContainerSize();
                final List<Node> subNodes = parseNodes(stream, subCount);
                node.children.addAll(subNodes);
                consumed += subCount;
            }
            nodes.add(node);
        }
        return nodes;
    }

    // --- GEZIELTE WAYPOINT-SUCHE IM BAUM ---
    // FK-TODO: refactor
    private static List<Node> findWaypointContainers(final List<Node> rootNodes, final int expectedCount) {
        final List<Node> all4mContainers = new ArrayList<>();
        findAllContainersByFieldId(rootNodes, 4, all4mContainers);

        for (final Node container : all4mContainers) {
            final List<Node> waypointChildren = new ArrayList<>();
            for (final Node child : container.children) {
                if (child.fieldId == 1 && child.isContainer()) {
                    waypointChildren.add(child);
                }
            }
            // Wir suchen den 4m-Container, der EXAKT so viele 1m-Kinder hat, wie wir Wegpunkte im Pfad gefunden haben
            if (waypointChildren.size() == expectedCount) {
                return waypointChildren;
            }
        }
        return List.of();
    }

    // FK-TODO: refactor
    private static void findAllContainersByFieldId(final List<Node> nodes, final int fieldId, final List<Node> result) {
        for (final Node node : nodes) {
            if (node.fieldId == fieldId && node.isContainer()) {
                result.add(node);
            }
            findAllContainersByFieldId(node.children, fieldId, result);
        }
    }

    // --- FLEXIBLE DATEN-EXTRAKTION AUS DEM SUB-BAUM ---
    // FK-TODO: refactor
    private static void extractDataFromSubtree(final Node node, final StopData stopData) {
        final Parser<String> placeIdParser = new PlaceIdParser();

        if (node.type == 's' && node.fieldId == 1) {
            if (placeIdParser.matches(node.token)) {
                stopData.placeId = Optional.of(placeIdParser.parse(node.token));
            }
        } else if (node.type == 'd') {
            try {
                final double value = Double.parseDouble(node.token.substring(String.valueOf(node.fieldId).length() + 1));

                // Flexibles Mapping für beide bekannten Google-Koordinatenformate (alt: 3d/4d, neu: 2d/1d)
                if (node.fieldId == 3 || node.fieldId == 2) {
                    stopData.latitude = Optional.of(value);
                } else if (node.fieldId == 4 || node.fieldId == 1) {
                    stopData.longitude = Optional.of(value);
                }
            } catch (final Exception ignored) {
            }
        }

        // Rekursiv tiefer suchen
        for (final Node child : node.children) {
            extractDataFromSubtree(child, stopData);
        }
    }

    // --- REPRÄSENTATION EINES PROTOBUF-KNOTENS ---
    // FK-TODO: refactor
    private static class Node {
        final String token;
        final int fieldId;
        final char type;
        final List<Node> children = new ArrayList<>();

        Node(final String token) {
            this.token = token;
            int typeIdx = 0;
            while (typeIdx < token.length() && Character.isDigit(token.charAt(typeIdx))) {
                typeIdx++;
            }
            if (typeIdx < token.length()) {
                this.fieldId = Integer.parseInt(token.substring(0, typeIdx));
                this.type = token.charAt(typeIdx);
            } else {
                this.fieldId = -1;
                this.type = '?';
            }
        }

        boolean isContainer() {
            return type == 'm';
        }

        int getContainerSize() {
            int typeIdx = 0;
            while (typeIdx < token.length() && Character.isDigit(token.charAt(typeIdx))) {
                typeIdx++;
            }
            if (typeIdx + 1 <= token.length()) {
                try {
                    return Integer.parseInt(token.substring(typeIdx + 1));
                } catch (final NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        }
    }
}