package de.knollfrank.extensionsformaps.route.protobuf;

import java.util.List;

public class NodeFactory {

    private NodeFactory() {
    }

    public static Node createContainer(final int fieldId, final List<Node> children) {
        return new Node(
                fieldId,
                Datatype.CONTAINER,
                String.valueOf(getTotalTokens(children)),
                children);
    }

    public static Node createLeaf(final int fieldId, final Datatype datatype, final String value) {
        return new Node(fieldId, datatype, value, List.of());
    }

    private static int getTotalTokens(final Node node) {
        return 1 + getTotalTokens(node.children());
    }

    private static int getTotalTokens(final List<Node> nodes) {
        return nodes
                .stream()
                .mapToInt(NodeFactory::getTotalTokens)
                .sum();
    }
}
