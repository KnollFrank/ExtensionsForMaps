package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodeParser {

    // --- REKURSIVER BAUM-PARSER (Tree Builder) ---
    // FK-TODO: refactor
    public static List<Node> parseAllNodes(final List<String> tokens) {
        return parseAllNodes(tokens.iterator());
    }

    private static List<Node> parseAllNodes(final Iterator<String> tokens) {
        final List<Node> nodes = new ArrayList<>();
        while (tokens.hasNext()) {
            final String token = tokens.next();
            final Node node = new Node(token);
            if (node.isContainer()) {
                final int subCount = node.getContainerSize();
                final List<Node> subNodes = parseNodes(tokens, subCount);
                node.children.addAll(subNodes);
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

            final Node node = new Node(token);
            if (node.isContainer()) {
                final int subCount = node.getContainerSize();
                final List<Node> subNodes = parseNodes(tokens, subCount);
                node.children.addAll(subNodes);
                consumed += subCount;
            }
            nodes.add(node);
        }
        return nodes;
    }
}
