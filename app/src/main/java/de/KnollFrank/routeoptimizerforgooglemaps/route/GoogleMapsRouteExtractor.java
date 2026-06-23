package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.common.Optionals;
import de.KnollFrank.routeoptimizerforgooglemaps.common.Strings;
import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;

public class GoogleMapsRouteExtractor {

	public static Route extractRouteFromDirectionsUrl(final URL directionsUrl) {
		if (!isDirectionsUrl(directionsUrl)) {
			throw new IllegalArgumentException(String.format("Invalid URL: %s is not a valid Google Maps directions URL.", directionsUrl));
		}

		final List<StopData> stopDataList = new ArrayList<>();
		final List<String> segments = SegmentsProvider.getSegments(directionsUrl);
		if (segments.isEmpty()) {
			return new Route(StopDataConverter.asStops(stopDataList));
		}

		int stopCounter = 1;
		for (final String segment : segments) {
			if (segment.isEmpty()) {
				continue;
			}
			final StopData stopData = new StopData();
			stopData.stopNumber = stopCounter++;
			stopData.pathName = URLs.decode(segment);
			if (segment.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+")) {
				final String[] coords = segment.split(",");
				stopData.lat = Optional.of(Double.parseDouble(coords[0]));
				stopData.lng = Optional.of(Double.parseDouble(coords[1]));
			}
			stopDataList.add(stopData);
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

						if (waypointIdx < stopDataList.size()) {
							final StopData stopData = stopDataList.get(waypointIdx);

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
		return new Route(StopDataConverter.asStops(stopDataList));
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

	private static class SegmentsProvider {

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
}