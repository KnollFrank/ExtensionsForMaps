package de.knollfrank.extensionsformaps.coordinate;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.StringJoiner;

public class Geodetic {

    private final Angle latitude;
    private final Angle longitude;

    private Geodetic(final Angle latitude, final Angle longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Geodetic fromLatitudeLongitude(final Angle latitude, final Angle longitude) {
        return new Geodetic(latitude, longitude);
    }

    public Angle getLatitude() {
        return latitude;
    }

    public Angle getLongitude() {
        return longitude;
    }

    public Geodetic add(final Geodetic other) {
        return Geodetic.fromLatitudeLongitude(
                this.latitude.add(other.latitude),
                this.longitude.add(other.longitude));
    }

    public Geodetic mul(final double factor) {
        return Geodetic.fromLatitudeLongitude(
                latitude.mul(factor),
                longitude.mul(factor));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Geodetic geodetic = (Geodetic) o;
        return latitude.equals(geodetic.latitude) &&
                longitude.equals(geodetic.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", Geodetic.class.getSimpleName() + "[", "]")
                .add("latitude=" + latitude)
                .add("longitude=" + longitude)
                .toString();
    }
}
