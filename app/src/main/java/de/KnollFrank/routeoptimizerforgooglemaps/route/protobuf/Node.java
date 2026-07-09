package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

// FK-TODO: refactor
public class Node {

    public final String token;
    // FK-TODO: fieldId und type in einer Markerklasse zusammenfassen, und hier als Optional<Marker> verwenden?
    public final int fieldId;
    // FK-TODO: use Datatype for type
    // FK-TODO: rename to dataType
    public final char type;
    private final String value;
    public final List<Node> children = new ArrayList<>();

    public Node(final int fieldId, final char type, String value) {
        this.token = String.valueOf(fieldId) + String.valueOf(type) + value;
        this.fieldId = fieldId;
        this.type = type;
        this.value = value;
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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Node node = (Node) o;
        return fieldId == node.fieldId && type == node.type && Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId, type, value);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("token='" + token + "'")
                .add("fieldId=" + fieldId)
                .add("type=" + type)
                .add("children=" + children)
                .toString();
    }
}
