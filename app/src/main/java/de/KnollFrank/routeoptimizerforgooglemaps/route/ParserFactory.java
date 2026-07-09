package de.KnollFrank.routeoptimizerforgooglemaps.route;

import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.Datatype;

class ParserFactory {

    public static Parser<Double> createLatitudeParser() {
        return createDoubleParser(MarkerFactory.createMarker(3, Datatype.DOUBLE));
    }

    public static Parser<Double> createLongitudeParser() {
        return createDoubleParser(MarkerFactory.createMarker(4, Datatype.DOUBLE));
    }

    private static Parser<Double> createDoubleParser(final String marker) {
        return new Parser<>() {

            @Override
            public boolean matches(final String token) {
                return token.startsWith(marker);
            }

            @Override
            public Double parse(final String token) {
                return Double.parseDouble(token.substring(marker.length()));
            }
        };
    }
}
