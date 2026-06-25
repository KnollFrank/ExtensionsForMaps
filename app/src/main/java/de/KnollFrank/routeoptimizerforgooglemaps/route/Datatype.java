package de.KnollFrank.routeoptimizerforgooglemaps.route;

enum Datatype {

    DOUBLE('d'),
    STRING('s'),
    CONTAINER('m');

    public final char marker;

    Datatype(final char marker) {
        this.marker = marker;
    }
}
