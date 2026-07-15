package de.KnollFrank.routeoptimizerforgooglemaps.route;

public record UndocumentedPlaceId(String value) {

    public UndocumentedPlaceId {
        if (!isUndocumentedPlaceId(value)) {
            throw new IllegalArgumentException(value);
        }
    }

    public OfficialPlaceId toOfficialPlaceId() {
        return PlaceIdConverter.toOfficialPlaceId(this);
    }

    private static boolean isUndocumentedPlaceId(final String value) {
        return value.contains(":");
    }
}
