package de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors;

import java.util.List;

public record OrsMatrixResponse(
        List<List<Double>> durations,
        List<List<Double>> distances) {
}
