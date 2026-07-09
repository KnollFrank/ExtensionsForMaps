package de.KnollFrank.routeoptimizerforgooglemaps.route;

public record Datatype(char marker) {

    public static final Datatype DOUBLE = new Datatype('d');
    public static final Datatype STRING = new Datatype('s');
    public static final Datatype CONTAINER = new Datatype('m');
}
