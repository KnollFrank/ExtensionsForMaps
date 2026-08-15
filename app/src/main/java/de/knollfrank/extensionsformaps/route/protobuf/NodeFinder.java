package de.knollfrank.extensionsformaps.route.protobuf;

import java.util.ArrayList;
import java.util.List;

public class NodeFinder {

    // FK-TODO: refactor
    public static List<Node> findWaypointContainers(final List<Node> rootNodes, final int expectedCount) {
        final List<Node> all4mContainers = new ArrayList<>();
        findAllContainersByFieldId(rootNodes, 4, all4mContainers);
        return all4mContainers
                .stream()
                .map(NodeFinder::getWaypointChildren)
                .filter(waypointChildrenOf4mContainer -> waypointChildrenOf4mContainer.size() == expectedCount)
                .findFirst()
                .orElse(List.of());
    }

    private static List<Node> getWaypointChildren(final Node node) {
        return node
                .children()
                .stream()
                .filter(NodeFinder::isWaypoint)
                .toList();
    }

    private static boolean isWaypoint(final Node node) {
        return node.fieldId() == 1 && node.isContainer();
    }

    // FK-TODO: refactor
    private static void findAllContainersByFieldId(final List<Node> nodes,
                                                   final int fieldId,
                                                   final List<Node> result) {
        for (final Node node : nodes) {
            if (node.fieldId() == fieldId && node.isContainer()) {
                result.add(node);
            }
            findAllContainersByFieldId(node.children(), fieldId, result);
        }
    }
}
