package de.KnollFrank.routeoptimizerforgooglemaps.route;

interface Parser<T> {

	boolean matches(String token);

	T parse(String token);
}
