package de.knollfrank.extensionsformaps.route.protobuf;

import java.util.ArrayList;
import java.util.List;

public class NodeFinder {

    // FK-TODO: refactor
    public static List<Node> findWaypointContainers(final List<Node> rootNodes, final int expectedCount) {
        final List<Node> all4mContainers = new ArrayList<>();
        findAllContainersByFieldId(rootNodes, 4, all4mContainers);
        for (final Node container4m : all4mContainers) {
            final List<Node> waypointChildrenOf4mContainer = new ArrayList<>();
            for (final Node child : container4m.children()) {
                if (child.fieldId() == 1 && child.isContainer()) {
                    waypointChildrenOf4mContainer.add(child);
                }
            }
            // Wir suchen den 4m-Container, der EXAKT so viele 1m-Kinder hat, wie wir Wegpunkte im Pfad gefunden haben
            if (waypointChildrenOf4mContainer.size() == expectedCount) {
                return waypointChildrenOf4mContainer;
            }
        }
        return List.of();
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
