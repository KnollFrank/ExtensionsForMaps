package de.KnollFrank.routeoptimizerforgooglemaps;

import static de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit.DEGREES;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public class GoogleMapsRouteExtractor {

	public record Route(List<Stop> stops) {
	}

	public record Stop(int stopNumber,
	                   String pathName,
	                   Optional<String> placeId,
	                   Geodetic geodetic) {
	}

	public static Route extractRouteFromDirectionsUrl(final URL directionsUrl) {
		if (!isDirectionsUrl(directionsUrl)) {
			throw new IllegalArgumentException(String.format("Invalid URL: %s is not a valid Google Maps directions URL.", directionsUrl));
		}

		final List<StopData> stops = new ArrayList<>();
		final String dirPathSegment = "/dir/";
		final int startIdx = directionsUrl.getPath().indexOf(dirPathSegment) + dirPathSegment.length();
		int endIdx = directionsUrl.getPath().contains("/data=") ? directionsUrl.getPath().indexOf("/data=") : directionsUrl.getPath().length();
		if (directionsUrl.getPath().contains("/@")) {
			endIdx = Math.min(endIdx, directionsUrl.getPath().indexOf("/@"));
		}
		final String pathPart = directionsUrl.getPath().substring(startIdx, endIdx);
		if (pathPart.isEmpty()) {
			return new Route(asStops(stops));
		}
		final String[] segments = pathPart.split("/");
		int stopCounter = 1;

		for (final String segment : segments) {
			if (segment.isEmpty()) {
				continue;
			}
			final StopData stopData = new StopData();
			stopData.stopNumber = stopCounter++;
			stopData.pathName = decode(segment);
			if (segment.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+")) {
				final String[] coords = segment.split(",");
				stopData.lat = Optional.of(Double.parseDouble(coords[0]));
				stopData.lng = Optional.of(Double.parseDouble(coords[1]));
			}
			stops.add(stopData);
		}

		if (directionsUrl.toString().contains("data=")) {
			String dataStr = directionsUrl.toString().split("data=")[1].split("&")[0];
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

						if (waypointIdx < stops.size()) {
							final StopData stopData = stops.get(waypointIdx);

							while (i < windowEnd && i < tokens.length) {
								String subToken = tokens[i++];
								if (subToken.startsWith("1s")) {
									stopData.placeId = Optional.of(convertHexToPlaceId(subToken.substring(2)));
								} else if (subToken.startsWith("3d")) {
									stopData.lat = Optional.of(Double.parseDouble(subToken.substring(2)));
								} else if (subToken.startsWith("4d")) {
									stopData.lng = Optional.of(Double.parseDouble(subToken.substring(2)));
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
		return new Route(asStops(stops));
	}

	private static class StopData {

		public int stopNumber;
		public String pathName;
		public Optional<String> placeId = Optional.empty();
		public Optional<Double> lat = Optional.empty();
		public Optional<Double> lng = Optional.empty();
	}

	private static boolean isDirectionsUrl(final URL url) {
		return "https".equals(url.getProtocol()) &&
				"www.google.com".equals(url.getHost()) &&
				url.getPath().startsWith("/maps/dir/");
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

	private static List<Stop> asStops(final List<StopData> stopDataList) {
		return stopDataList
				.stream()
				.map(GoogleMapsRouteExtractor::asStop)
				.toList();
	}

	private static Stop asStop(final StopData stopData) {
		return new Stop(
				stopData.stopNumber,
				stopData.pathName,
				stopData.placeId,
				Geodetic.fromLatitudeLongitude(
						new Angle(stopData.lat.orElseThrow(), DEGREES),
						new Angle(stopData.lng.orElseThrow(), DEGREES)));
	}

	private static String decode(final String s) {
		try {
			return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}