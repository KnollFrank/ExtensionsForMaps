package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodesParser {

    // FK-TODO: refactor
    public static List<Node> parseAllNodes(final List<String> tokens) {
        return parseAllNodes(tokens.iterator());
    }

    private static List<Node> parseAllNodes(final Iterator<String> tokens) {
        final List<Node> nodes = new ArrayList<>();
        while (tokens.hasNext()) {
            final String token = tokens.next();
            final Node node = NodeParser.parseNode(token);
            if (node.isContainer()) {
                parseTokensAsChildrenOfNode(node, tokens);
            }
            nodes.add(node);
        }
        return nodes;
    }

    // FK-TODO: refactor
    private static List<Node> parseNodes(final Iterator<String> tokens, final int toConsume) {
        final List<Node> nodes = new ArrayList<>();
        int consumed = 0;
        while (consumed < toConsume && tokens.hasNext()) {
            final String token = tokens.next();
            consumed++;
            final Node node = NodeParser.parseNode(token);
            if (node.isContainer()) {
                parseTokensAsChildrenOfNode(node, tokens);
                consumed += node.getContainerSize();
            }
            nodes.add(node);
        }
        return nodes;
    }

    private static void parseTokensAsChildrenOfNode(final Node node, final Iterator<String> tokens) {
        node.children.addAll(parseNodes(tokens, node.getContainerSize()));
    }
}
