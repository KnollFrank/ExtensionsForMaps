package de.knollfrank.extensionsformaps.optimize.osrm;

import java.util.List;

public record OsrmTableResponse(
        String code,
        List<List<Double>> distances,
        List<List<Double>> durations) {
}
