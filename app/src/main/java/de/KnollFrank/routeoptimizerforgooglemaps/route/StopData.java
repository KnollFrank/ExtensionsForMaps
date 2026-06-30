package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Optional;

class StopData {

    public final String id;
    public final String address;
    public Optional<String> placeId = Optional.empty();
    public Optional<Double> latitude = Optional.empty();
    public Optional<Double> longitude = Optional.empty();

    public StopData(final String address, final String id) {
        this.id = id;
        this.address = address;
    }
}
