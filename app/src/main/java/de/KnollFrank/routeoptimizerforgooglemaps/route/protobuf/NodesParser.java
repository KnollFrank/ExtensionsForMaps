package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodesParser {

    // FK-TODO: refactor
    public static List<Node> parseNodes(final List<String> tokens) {
        return new Worker(tokens.iterator()).parseNodes();
    }

    private static class Worker {

        private final Iterator<String> tokens;

        public Worker(final Iterator<String> tokens) {
            this.tokens = tokens;
        }

        public List<Node> parseNodes() {
            final ImmutableList.Builder<Node> nodesBuilder = ImmutableList.builder();
            while (tokens.hasNext()) {
                final String token = tokens.next();
                nodesBuilder.add(parseNode(token).node());
            }
            return nodesBuilder.build();
        }

        private record NodeAndConsumedTokens(Node node, int consumedTokens) {
        }

        private NodeAndConsumedTokens parseNodeWithChildren(final Node node) {
            return node.isContainer() ?
                    new NodeAndConsumedTokens(
                            new Node(
                                    node.fieldId,
                                    node.datatype,
                                    node.value,
                                    parseNodes(node.getContainerSize())),
                            node.getContainerSize()) :
                    new NodeAndConsumedTokens(node, 0);
        }

        // FK-TODO: refactor
        private List<Node> parseNodes(final int toConsume) {
            final List<Node> nodes = new ArrayList<>();
            int consumed = 0;
            while (consumed < toConsume) {
                final String token = tokens.next();
                consumed++;
                final NodeAndConsumedTokens nodeAndConsumedTokens = parseNode(token);
                consumed += nodeAndConsumedTokens.consumedTokens();
                nodes.add(nodeAndConsumedTokens.node());
            }
            return nodes;
        }

        private NodeAndConsumedTokens parseNode(final String token) {
            return parseNodeWithChildren(NodeParser.parseNodeWithoutChildren(token));
        }
    }
}
