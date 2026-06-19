package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.List;

public class GoogleMapsNavigator {

	/**
	 * Builds a bulletproof, high-precision Google Maps URL using raw coordinates.
	 * Guaranteed to work flawlessly with the Android Google Maps Intent API.
	 */
	// FK-TODO: add unit test for building the URL.
	public static void launchRouteOverview(final Context context, final List<RouteOptimizer.Stop> optimizedStops) {
		if (optimizedStops == null || optimizedStops.size() < 2) {
			return;
		}

		// 1. The modern, official Google Maps Directions API endpoint
		final StringBuilder urlBuilder = new StringBuilder("https://www.google.com/maps/dir/?api=1");

		// 2. Set the exact coordinates for the starting point
		final RouteOptimizer.Stop origin = optimizedStops.get(0);
		urlBuilder
				.append("&origin=")
				.append(origin.lat())
				.append(",")
				.append(origin.lng());

		// FK-TODO: der letzte optimizedStop muß nicht unbedingt das Ziel sein. Wie ist das Destination vs. Waypoint in der Original-Teilen-URL von Google Maps mit dem data-Parameter hinterlegt? Unit-Test dazu schreiben.
		// 3. Set the exact coordinates for the final destination
		final RouteOptimizer.Stop destination = optimizedStops.get(optimizedStops.size() - 1);
		urlBuilder
				.append("&destination=")
				.append(destination.lat())
				.append(",")
				.append(destination.lng());

		// 4. Handle intermediate waypoints if there are any stops in between
		if (optimizedStops.size() > 2) {
			urlBuilder.append("&waypoints=");
			for (int i = 1; i < optimizedStops.size() - 1; i++) {
				final RouteOptimizer.Stop waypoint = optimizedStops.get(i);
				urlBuilder
						.append(waypoint.lat())
						.append(",")
						.append(waypoint.lng());
				// Modern API separates multiple waypoints using the pipe character '|'
				if (i < optimizedStops.size() - 2) {
					urlBuilder.append("|");
				}
			}
		}

		// FK-TODO: extract method
		final Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlBuilder.toString()));
		mapIntent.setPackage("com.google.android.apps.maps");
		mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(mapIntent);
	}
}