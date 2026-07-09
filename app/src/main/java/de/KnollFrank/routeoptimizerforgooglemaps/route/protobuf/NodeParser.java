package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

public class NodeParser {

    // FK-TODO: refactor
    // token = [fieldId][dataType][value]
    public static Node parseNode(final String token) {
        final String fieldId = getFieldId(token);
        return new Node(
                Integer.parseInt(fieldId),
                token.charAt(fieldId.length()),
                token.substring(fieldId.length() + 1));
    }

    private static String getFieldId(final String token) {
        int typeIdx = 0;
        while (typeIdx < token.length() && Character.isDigit(token.charAt(typeIdx))) {
            typeIdx++;
        }
        return token.substring(0, typeIdx);
    }
}
