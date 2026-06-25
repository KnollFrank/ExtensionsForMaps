package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;

class SegmentToStopDataFromConverter {

    public static List<StopData> convert(final List<String> segments) {
        return Lists
                .asIndexedElements(segments)
                .stream()
                .map(indexedSegment -> convert(indexedSegment.element(), indexedSegment.index() + 1))
                .toList();
    }

    private static StopData convert(final String segment, final int stopNumber) {
        final StopData stopData = new StopData();
        // FK-TODO: stopNumber und pathName als Konstruktorparameter von StopData übergeben
        stopData.stopNumber = stopNumber;
        stopData.pathName = URLs.decode(segment);
        if (segment.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+")) {
            final String[] coords = segment.split(",");
            stopData.latitude = Optional.of(Double.parseDouble(coords[0]));
            stopData.longitude = Optional.of(Double.parseDouble(coords[1]));
        }
        return stopData;
    }
}
