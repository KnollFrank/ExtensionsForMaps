package de.knollfrank.extensionsformaps.optimize.ors;

import java.util.List;

public record OrsMatrixResponse(
        List<List<Double>> durations,
        List<List<Double>> distances) {
}
