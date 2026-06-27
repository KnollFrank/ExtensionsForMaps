package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import java.util.Arrays;
import java.util.StringJoiner;

// Interner Container für die OSRM Distanz- und Dauer-Matrizen
public record RoutingMatrices(double[][] distances, double[][] durations) {

    @Override
    public String toString() {
        return new StringJoiner(", ", RoutingMatrices.class.getSimpleName() + "[", "]")
                .add("distances=" + Arrays.deepToString(distances))
                .add("durations=" + Arrays.deepToString(durations))
                .toString();
    }
}
