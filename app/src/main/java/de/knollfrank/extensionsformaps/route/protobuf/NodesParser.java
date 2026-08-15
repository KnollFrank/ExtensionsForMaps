package de.knollfrank.extensionsformaps.route.protobuf;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

public class NodesParser {

    public static List<Node> parseNodes(final List<String> tokens) {
        return new Worker(tokens.iterator()).parseNodes();
    }

    private static class Worker {

        private final Iterator<String> tokens;

        public Worker(final Iterator<String> tokens) {
            this.tokens = tokens;
        }

        private record NodeAndConsumedTokens(Node node, int consumedTokens) {

            public NodeAndConsumedTokens addConsumedTokens(final int consumedTokens) {
                return new NodeAndConsumedTokens(
                        node(),
                        consumedTokens() + consumedTokens);
            }
        }

        public List<Node> parseNodes() {
            final ImmutableList.Builder<Node> nodesBuilder = ImmutableList.builder();
            while (tokens.hasNext()) {
                nodesBuilder.add(parseNode().node());
            }
            return nodesBuilder.build();
        }

        private NodeAndConsumedTokens parseNode() {
            return this
                    .addChildrenToNode(NodeParser.parseNode(tokens.next()))
                    .addConsumedTokens(1);
        }

        private NodeAndConsumedTokens addChildrenToNode(final Node node) {
            return node.isContainer() ?
                    new NodeAndConsumedTokens(
                            new Node(
                                    node.fieldId(),
                                    node.datatype(),
                                    node.value(),
                                    parseNodes(node.getContainerSize())),
                            node.getContainerSize()) :
                    new NodeAndConsumedTokens(node, 0);
        }

        private List<Node> parseNodes(final int toConsume) {
            final ImmutableList.Builder<Node> nodesBuilder = ImmutableList.builder();
            int consumed = 0;
            while (consumed != toConsume) {
                final NodeAndConsumedTokens nodeAndConsumedTokens = parseNode();
                consumed += nodeAndConsumedTokens.consumedTokens();
                nodesBuilder.add(nodeAndConsumedTokens.node());
            }
            return nodesBuilder.build();
        }
    }
}
