package de.knollfrank.extensionsformaps.route.protobuf;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

public class NodeSerializer {

    public static String serialize(final List<Node> nodes) {
        return NodeSerializer
                ._serialize(nodes)
                .stream()
                .collect(Collectors.joining("!", "!", ""));
    }

    private static List<String> _serialize(final List<Node> nodes) {
        return nodes
                .stream()
                .map(NodeSerializer::_serialize)
                .toList();
    }

    private static String _serialize(final Node node) {
        return String.join(
                "!",
                ImmutableList
                        .<String>builder()
                        .add(node.getToken())
                        .addAll(_serialize(node.children()))
                        .build());
    }
}
