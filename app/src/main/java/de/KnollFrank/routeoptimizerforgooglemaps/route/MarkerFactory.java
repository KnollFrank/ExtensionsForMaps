package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Locale;

import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.Datatype;

class MarkerFactory {

    public static String createMarker(final int fieldId, final Datatype dataType) {
        return String.format(Locale.ROOT, "%d%c", fieldId, dataType.marker());
    }
}
