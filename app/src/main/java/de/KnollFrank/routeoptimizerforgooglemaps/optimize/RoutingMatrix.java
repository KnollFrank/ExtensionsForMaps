package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

public class RoutingMatrix {

    private final ImmutableTable<Stop, Stop, DistanceDuration> distanceDurationByStopTable;
    private Optional<ImmutableTable<String, String, DistanceDuration>> distanceDurationByStopIdTable = Optional.empty();

    public RoutingMatrix(final ImmutableTable<Stop, Stop, DistanceDuration> distanceDurationByStopTable) {
        if (!distanceDurationByStopTable.rowKeySet().equals(distanceDurationByStopTable.columnKeySet())) {
            throw new IllegalArgumentException("" + distanceDurationByStopTable);
        }
        this.distanceDurationByStopTable = distanceDurationByStopTable;
    }

    public ImmutableTable<Stop, Stop, DistanceDuration> getDistanceDurationByStopTable() {
        return distanceDurationByStopTable;
    }

    public ImmutableTable<String, String, DistanceDuration> getDistanceDurationByStopIdTable() {
        if (distanceDurationByStopIdTable.isEmpty()) {
            distanceDurationByStopIdTable = Optional.of(computeDistanceDurationByStopIdTable(distanceDurationByStopTable));
        }
        return distanceDurationByStopIdTable.orElseThrow();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final RoutingMatrix that = (RoutingMatrix) o;
        return Objects.equals(distanceDurationByStopTable, that.distanceDurationByStopTable);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(distanceDurationByStopTable);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", RoutingMatrix.class.getSimpleName() + "[", "]")
                .add("distanceDurationByStopTable=" + distanceDurationByStopTable)
                .toString();
    }

    private static ImmutableTable<String, String, DistanceDuration> computeDistanceDurationByStopIdTable(final ImmutableTable<Stop, Stop, DistanceDuration> distanceDurationByStopTable) {
        return distanceDurationByStopTable
                .cellSet()
                .stream()
                .collect(
                        ImmutableTable.toImmutableTable(
                                cell -> cell.getRowKey().id(),
                                cell -> cell.getColumnKey().id(),
                                Table.Cell::getValue));
    }
}
