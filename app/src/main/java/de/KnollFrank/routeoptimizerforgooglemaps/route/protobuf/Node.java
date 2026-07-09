package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

// FK-TODO: refactor
public class Node {

    // FK-TODO: fieldId und type in einer Markerklasse zusammenfassen, und hier als Optional<Marker> verwenden?
    public final int fieldId;
    // FK-TODO: use Datatype for type
    public final char dataType;
    private final String value;
    public final List<Node> children = new ArrayList<>();

    public Node(final int fieldId, final char dataType, String value) {
        this.fieldId = fieldId;
        this.dataType = dataType;
        this.value = value;
    }

    public String getToken() {
        return String.valueOf(fieldId) + String.valueOf(dataType) + value;
    }

    public boolean isContainer() {
        return dataType == 'm';
    }

    public int getContainerSize() {
        return Integer.parseInt(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Node node = (Node) o;
        return fieldId == node.fieldId && dataType == node.dataType && Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId, dataType, value);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("fieldId=" + fieldId)
                .add("dataType=" + dataType)
                .add("value='" + value + "'")
                .toString();
    }
}
