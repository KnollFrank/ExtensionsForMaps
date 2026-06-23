package de.KnollFrank.routeoptimizerforgooglemaps.route;

enum Datatype {
	DOUBLE("d"),
	STRING("s"),
	CONTAINER("m");

	public final String marker;

	Datatype(final String marker) {
		this.marker = marker;
	}
}
