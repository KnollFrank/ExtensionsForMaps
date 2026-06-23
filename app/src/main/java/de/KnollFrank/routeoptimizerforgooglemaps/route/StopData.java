package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Optional;

class StopData {

	public int stopNumber;
	public String pathName;
	public Optional<String> placeId = Optional.empty();
	public Optional<Double> lat = Optional.empty();
	public Optional<Double> lng = Optional.empty();
}
