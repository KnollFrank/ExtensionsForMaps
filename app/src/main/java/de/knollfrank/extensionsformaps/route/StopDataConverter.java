package de.knollfrank.extensionsformaps.route;

import static de.knollfrank.extensionsformaps.coordinate.Unit.DEGREES;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;

class StopDataConverter {

    public static List<Stop> asStops(final List<StopData> stopDataList) {
        return stopDataList
                .stream()
                .map(StopDataConverter::asStop)
                .toList();
    }

    public static Stop asStop(final StopData stopData) {
        return new Stop(
                stopData.id,
                stopData.address,
                stopData.officialPlaceId,
                Geodetic.fromLatitudeLongitude(
                        new Angle(
                                stopData.latitude.orElseThrow(() -> createMissingCoordinateException("latitude", stopData)),
                                DEGREES),
                        new Angle(
                                stopData.longitude.orElseThrow(() -> createMissingCoordinateException("longitude", stopData)),
                                DEGREES)),
                Optional.empty());
    }

    private static IllegalArgumentException createMissingCoordinateException(
            final String missingValue,
            final StopData stopData) {
        return new IllegalArgumentException(
                String.format(
                        Locale.ROOT,
                        "Missing %s for stop %s ('%s'). Link expansion might have failed.",
                        missingValue,
                        stopData.id,
                        stopData.address));
    }

}
