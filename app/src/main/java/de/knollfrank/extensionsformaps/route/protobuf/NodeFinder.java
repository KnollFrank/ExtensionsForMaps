package de.knollfrank.extensionsformaps.route.protobuf;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Lists;

public class NodeFinder {

    public static Optional<List<Node>> findWaypointContainers(final List<Node> rootNodes, final int expectedCount) {
        return NodeFinder
                .getAllNodes(rootNodes)
                .stream()
                .filter(NodeFinder::is4mContainer)
                .map(NodeFinder::getWaypointChildren)
                .filter(waypointChildrenOf4mContainer -> waypointChildrenOf4mContainer.size() == expectedCount)
                .findFirst();
    }

    private static List<Node> getAllNodes(final List<Node> rootNodes) {
        return Lists.concat(
                rootNodes
                        .stream()
                        .map(Nodes::getAllNodes)
                        .toList());
    }

    private static List<Node> getWaypointChildren(final Node node) {
        return node
                .children()
                .stream()
                .filter(NodeFinder::isWaypoint)
                .toList();
    }

    private static boolean is4mContainer(final Node node) {
        return isContainerHavingFieldId(node, 4);
    }

    private static boolean isWaypoint(final Node node) {
        return isContainerHavingFieldId(node, 1);
    }

    private static boolean isContainerHavingFieldId(final Node node, final int fieldId) {
        return node.isContainer() && node.fieldId() == fieldId;
    }
}
