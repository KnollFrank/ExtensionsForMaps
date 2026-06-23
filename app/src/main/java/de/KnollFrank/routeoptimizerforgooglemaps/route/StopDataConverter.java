package de.KnollFrank.routeoptimizerforgooglemaps.route;

import static de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit.DEGREES;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

class StopDataConverter {

	public static List<Stop> asStops(final List<StopData> stopDataList) {
		return stopDataList
				.stream()
				.map(StopDataConverter::asStop)
				.toList();
	}

	public static Stop asStop(final StopData stopData) {
		return new Stop(
				stopData.stopNumber,
				stopData.pathName,
				stopData.placeId,
				Geodetic.fromLatitudeLongitude(
						new Angle(stopData.latitude.orElseThrow(), DEGREES),
						new Angle(stopData.longitude.orElseThrow(), DEGREES)));
	}
}
