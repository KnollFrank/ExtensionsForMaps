package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Datatype;

// FK-TODO: refactor
public class Node {

    public final int fieldId;
    // FK-TODO: use Datatype?
    public final char dataType;
    public final String value;
    // FK-TODO: use ImmutableValueGraph instead of of children
    public final List<Node> children = new ArrayList<>();

    public Node(final int fieldId, final char dataType, final String value) {
        this.fieldId = fieldId;
        this.dataType = dataType;
        this.value = value;
    }

    public String getToken() {
        return "" + fieldId + dataType + value;
    }

    public boolean isContainer() {
        return dataType == Datatype.CONTAINER.marker();
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
