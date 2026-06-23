package de.KnollFrank.routeoptimizerforgooglemaps.coordinate;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Angle {

	private final double angleInRadians;

	public Angle(final double angle, final Unit unit) {
		this.angleInRadians = getAngleInRadians(angle, unit);
	}

	public double toDegrees() {
		return Math.toDegrees(angleInRadians);
	}

	public double toRadians() {
		return angleInRadians;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Angle angle = (Angle) o;
		return Double.compare(angle.angleInRadians, angleInRadians) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(angleInRadians);
	}

	@NonNull
	@Override
	public String toString() {
		return "Angle{angle = " + toDegrees() + "°}";
	}

	private static double getAngleInRadians(final double angle, final Unit unit) {
		return switch (unit) {
			case DEGREES -> Math.toRadians(angle);
			case RADIANS -> angle;
		};
	}
}
