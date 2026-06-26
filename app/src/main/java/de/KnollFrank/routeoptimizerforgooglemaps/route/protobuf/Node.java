package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import java.util.ArrayList;
import java.util.List;

// FK-TODO: refactor
public class Node {

    public final String token;
    // FK-TODO: fieldId und type in einer Markerklasse zusammenfassen, und hier als Optional<Marker> verwenden?
    public final int fieldId;
    // FK-TODO: use Datatype for type
    public final char type;
    public final List<Node> children = new ArrayList<>();

    public Node(final String token) {
        this.token = token;
        int typeIdx = 0;
        while (typeIdx < token.length() && Character.isDigit(token.charAt(typeIdx))) {
            typeIdx++;
        }
        if (typeIdx < token.length()) {
            this.fieldId = Integer.parseInt(token.substring(0, typeIdx));
            this.type = token.charAt(typeIdx);
        } else {
            this.fieldId = -1;
            this.type = '?';
        }
    }

    public boolean isContainer() {
        return type == 'm';
    }

    public int getContainerSize() {
        int typeIdx = 0;
        while (typeIdx < token.length() && Character.isDigit(token.charAt(typeIdx))) {
            typeIdx++;
        }
        if (typeIdx + 1 <= token.length()) {
            try {
                return Integer.parseInt(token.substring(typeIdx + 1));
            } catch (final NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
