package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

public class NodeParser {

    // FK-TODO: refactor
    public static Node parseNode(final String token) {
        int fieldId;
        final char type;
        int typeIdx = 0;
        while (typeIdx < token.length() && Character.isDigit(token.charAt(typeIdx))) {
            typeIdx++;
        }
        if (typeIdx < token.length()) {
            fieldId = Integer.parseInt(token.substring(0, typeIdx));
            type = token.charAt(typeIdx);
        } else {
            fieldId = -1;
            type = '?';
        }
        return new Node(token, fieldId, type);
    }
}
