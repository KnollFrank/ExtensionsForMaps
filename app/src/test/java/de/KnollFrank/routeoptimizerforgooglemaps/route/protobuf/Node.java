package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public record Node(int fieldId,
                   Datatype datatype,
                   String value,
                   // FK-TODO: use ImmutableValueGraph instead of children?
                   List<Node> children) {

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
