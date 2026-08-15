package de.knollfrank.extensionsformaps.route.protobuf;

import com.google.common.collect.ImmutableList;

import java.util.List;

import de.knollfrank.extensionsformaps.common.Lists;

class Nodes {

    private Nodes() {
    }

    public static List<Node> getAllNodes(final Node node) {
        return ImmutableList
                .<Node>builder()
                .add(node)
                .addAll(
                        Lists.concat(
                                node
                                        .children()
                                        .stream()
                                        .map(Nodes::getAllNodes)
                                        .toList()))
                .build();
    }
}
