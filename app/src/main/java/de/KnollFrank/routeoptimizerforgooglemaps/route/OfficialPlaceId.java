package de.KnollFrank.routeoptimizerforgooglemaps.route;

import androidx.annotation.NonNull;

import java.util.Objects;

public class OfficialPlaceId {

    private final String value;

    public OfficialPlaceId(final String value) {
        if (!isOfficialPlaceId(value)) {
            throw new IllegalArgumentException(value);
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    public UndocumentedPlaceId toUndocumentedPlaceId() {
        return PlaceIdConverter.toUndocumentedPlaceId(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof OfficialPlaceId that)) return false;
        // Logischer Vergleich ab dem 5. Zeichen (ignoriert ChIJ vs GhIJ)
        return withoutPrefix(this.value).equals(withoutPrefix(that.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(withoutPrefix(value));
    }

    @NonNull
    @Override
    public String toString() {
        return "OfficialPlaceId{value='" + value + "'}";
    }

    private static final String CHIJ_PREFIX = "ChIJ";
    private static final String GHIJ_PREFIX = "GhIJ";

    private static boolean isOfficialPlaceId(final String value) {
        return value.length() == 27 && (value.startsWith(CHIJ_PREFIX) || value.startsWith(GHIJ_PREFIX));
    }

    private static String withoutPrefix(final String value) {
        return value.substring(CHIJ_PREFIX.length());
    }
}
