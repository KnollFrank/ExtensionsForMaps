package de.KnollFrank.routeoptimizerforgooglemaps.route;

public record OfficialPlaceId(String value) {

    public OfficialPlaceId {
        if (!isOfficialPlaceId(value)) {
            throw new IllegalArgumentException(value);
        }
    }

    public UndocumentedPlaceId toUndocumentedPlaceId() {
        return PlaceIdConverter.toUndocumentedPlaceId(this);
    }

    public static boolean isOfficialPlaceId(final String value) {
        return value.length() == 27 && (value.startsWith("ChI") || value.startsWith("GhI"));
    }
}
