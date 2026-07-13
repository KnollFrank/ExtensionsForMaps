package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import java.util.List;

class NodeParser {

    // FK-TODO: refactor
    // token = [fieldId][dataType][value]
    public static Node parseNodeWithoutChildren(final String token) {
        final String fieldId = getFieldId(token);
        return new Node(
                Integer.parseInt(fieldId),
                new Datatype(token.charAt(fieldId.length())),
                token.substring(fieldId.length() + 1),
                List.of());
    }

    private static String getFieldId(final String token) {
        int index = 0;
        while (index < token.length() && Character.isDigit(token.charAt(index))) {
            index++;
        }
        return token.substring(0, index);
    }
}
