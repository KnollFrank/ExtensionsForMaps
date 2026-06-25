package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Optional;

class StopData {

    public final int stopNumber;
    public final String pathName;
    public Optional<String> placeId = Optional.empty();
    public Optional<Double> latitude = Optional.empty();
    public Optional<Double> longitude = Optional.empty();

    public StopData(final int stopNumber, final String pathName) {
        this.stopNumber = stopNumber;
        this.pathName = pathName;
    }
}
