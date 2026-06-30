package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import java.util.Arrays;
import java.util.StringJoiner;

// Interner Container für die OSRM Distanz- und Dauer-Matrizen
// FK-TODO: use com.google.common.collect.Table<Stop, Stop, Double> instead of double[][]. Dazu die Parameter des RoutingMatricesProvider von Geodetic auf Stop ändern. Ev. auch nicht einen einzigen Double in der Table speichern sondern zwei Doubles für distance und duration.
public record RoutingMatrices(double[][] distances, double[][] durations) {

    @Override
    public String toString() {
        return new StringJoiner(", ", RoutingMatrices.class.getSimpleName() + "[", "]")
                .add("distances=" + Arrays.deepToString(distances))
                .add("durations=" + Arrays.deepToString(durations))
                .toString();
    }
}
