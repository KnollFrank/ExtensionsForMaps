package de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors;

import java.util.List;

public record OrsMatrixRequest(
        List<List<Double>> locations,
        List<String> metrics,
        String units) {
}
