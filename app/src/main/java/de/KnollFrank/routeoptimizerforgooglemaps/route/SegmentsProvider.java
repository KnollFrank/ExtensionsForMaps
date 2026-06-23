package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.common.Optionals;
import de.KnollFrank.routeoptimizerforgooglemaps.common.Strings;

class SegmentsProvider {

	public static List<String> getSegments(final URL directionsUrl) {
		return Lists.toList(
				SegmentsProvider
						.getPathPart(directionsUrl.getPath())
						.split("/"));
	}

	private static String getPathPart(final String path) {
		return path.substring(getStartIndex(path), getEndIndex(path));
	}

	private static int getStartIndex(final String path) {
		final String dirPathSegment = "/dir/";
		return Strings.indexOf(path, dirPathSegment).orElseThrow() + dirPathSegment.length();
	}

	private static int getEndIndex(final String path) {
		final int endIndex =
				Strings
						.indexOf(path, "/data=")
						.orElseGet(path::length);
		return Optionals
				.asOptional(Strings.indexOf(path, "/@"))
				.map(indexOfAddSegment -> Math.min(endIndex, indexOfAddSegment))
				.orElse(endIndex);
	}
}
