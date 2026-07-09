package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

// FK-TODO: refactor
public class Node {

    public final int fieldId;
    public final Datatype datatype;
    public final String value;
    // FK-TODO: use ImmutableValueGraph instead of of children
    public final List<Node> children = new ArrayList<>();

    public Node(final int fieldId, final Datatype datatype, final String value) {
        this.fieldId = fieldId;
        this.datatype = datatype;
        this.value = value;
    }

    public String getToken() {
        return "" + fieldId + datatype.marker() + value;
    }

    public boolean isContainer() {
        return Datatype.CONTAINER.equals(datatype);
    }

    public int getContainerSize() {
        return Integer.parseInt(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Node node = (Node) o;
        return fieldId == node.fieldId && Objects.equals(datatype, node.datatype) && Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId, datatype, value);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("fieldId=" + fieldId)
                .add("datatype=" + datatype)
                .add("value='" + value + "'")
                .toString();
    }
}
