package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Optional;

class StopData {

    public final String address;
    public final int stopNumber;
    public Optional<String> placeId = Optional.empty();
    public Optional<Double> latitude = Optional.empty();
    public Optional<Double> longitude = Optional.empty();

    public StopData(final String address, final int stopNumber) {
        this.address = address;
        this.stopNumber = stopNumber;
    }
}
