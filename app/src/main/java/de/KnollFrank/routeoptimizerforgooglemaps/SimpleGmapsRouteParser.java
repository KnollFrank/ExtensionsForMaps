package de.KnollFrank.routeoptimizerforgooglemaps;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SimpleGmapsRouteParser {

	/**
	 * Thrown when a stop (start, waypoint, or destination) is missing
	 * latitude and longitude data in the provided URL.
	 */
	public static class MissingCoordinateException extends RuntimeException {

		public MissingCoordinateException(final String message) {
			super(message);
		}
	}

	public static class Waypoint {

		public int stopNumber;
		public String pathName;
		public String placeId = null;
		public Double lat = null;
		public Double lng = null;

		@NonNull
		@Override
		public String toString() {
			return String.format(
					"Stop %d [%s] -> PlaceID: %s, Lat: %s, Lng: %s",
					stopNumber, pathName, placeId, lat, lng);
		}
	}

	public static List<Waypoint> parseRoute(String urlStr) throws MissingCoordinateException {
		List<Waypoint> waypoints = new ArrayList<>();

		if (urlStr == null || urlStr.trim().isEmpty()) {
			return waypoints;
		}

		if (!urlStr.contains("/dir/")) {
			throw new IllegalArgumentException(
					"Invalid URL: This is not a valid Google Maps directions URL " +
							"(the '/dir/' segment is missing)."
			);
		}

		final int startIdx = urlStr.indexOf("/dir/") + 5;
		int endIdx = urlStr.contains("/data=") ? urlStr.indexOf("/data=") : urlStr.length();
		if (urlStr.contains("/@")) {
			endIdx = Math.min(endIdx, urlStr.indexOf("/@"));
		}
		final String pathPart = urlStr.substring(startIdx, endIdx);
		if (pathPart.isEmpty()) {
			return waypoints;
		}
		final String[] segments = pathPart.split("/");
		int stopCounter = 1;

		for (final String segment : segments) {
			if (segment.isEmpty()) continue;
			final Waypoint wp = new Waypoint();
			wp.stopNumber = stopCounter++;

			// FK-TODO: extract method
			try {
				wp.pathName = URLDecoder.decode(segment, StandardCharsets.UTF_8.name());
			} catch (final UnsupportedEncodingException e) {
				// Fallback, falls UTF-8 absolut unerwartet nicht unterstützt wird
				wp.pathName = segment;
			}

			if (segment.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+")) {
				final String[] coords = segment.split(",");
				wp.lat = Double.parseDouble(coords[0]);
				wp.lng = Double.parseDouble(coords[1]);
			}
			waypoints.add(wp);
		}

		if (urlStr.contains("data=")) {
			String dataStr = urlStr.split("data=")[1].split("&")[0];
			String[] tokens = dataStr.startsWith("!") ? dataStr.substring(1).split("!") : dataStr.split("!");

			int waypointIdx = 0;
			int i = 0;

			while (i < tokens.length) {
				String token = tokens[i++];

				if (token.startsWith("1m") && token.length() == 3) {
					char subTokenCountChar = token.charAt(2);

					if (subTokenCountChar == '0' || subTokenCountChar == '2' || subTokenCountChar == '5') {
						int subTokens = Character.getNumericValue(subTokenCountChar);
						int windowEnd = i + subTokens;

						if (waypointIdx < waypoints.size()) {
							Waypoint wp = waypoints.get(waypointIdx);

							while (i < windowEnd && i < tokens.length) {
								String subToken = tokens[i++];
								if (subToken.startsWith("1s")) {
									wp.placeId = convertHexToPlaceId(subToken.substring(2));
								} else if (subToken.startsWith("3d")) {
									wp.lat = Double.parseDouble(subToken.substring(2));
								} else if (subToken.startsWith("4d")) {
									wp.lng = Double.parseDouble(subToken.substring(2));
								}
							}
							waypointIdx++;
						} else {
							i = windowEnd;
						}
					}
				}
			}
		}

		for (final Waypoint wp : waypoints) {
			if (wp.lat == null || wp.lng == null) {
				throw new MissingCoordinateException(
						"Missing coordinates for Stop " + wp.stopNumber + " ('" + wp.pathName + "'). " +
								"A GPS fallback is strictly required for this type of Google Maps URL."
				);
			}
		}

		return waypoints;
	}

	/**
	 * Converts an internal Google Maps Hex-ID into a standard Web-API Place ID ("ChIJ...").
	 */
	private static String convertHexToPlaceId(final String internalId) {
		if (internalId == null || !internalId.contains(":")) {
			return internalId;
		}

		try {
			final String[] parts = internalId.split(":");
			long cellId = Long.parseUnsignedLong(parts[0].replace("0x", ""), 16);
			long featureId = Long.parseUnsignedLong(parts[1].replace("0x", ""), 16);

			byte[] proto = new byte[20];
			proto[0] = 0x0A;
			proto[1] = 0x12;
			proto[2] = 0x09;

			for (int i = 0; i < 8; i++) {
				proto[3 + i] = (byte) (cellId >> (8 * i));
			}

			proto[11] = 0x11;

			for (int i = 0; i < 8; i++) {
				proto[12 + i] = (byte) (featureId >> (8 * i));
			}

			return Base64.getUrlEncoder().withoutPadding().encodeToString(proto);
		} catch (final Exception e) {
			return internalId;
		}
	}
}