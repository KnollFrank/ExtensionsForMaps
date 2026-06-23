package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
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
		final List<String> segments = getNonEmptySegments(directionsUrl);
		if (segments.isEmpty()) {
			return new Route(List.of());
		}
		final List<StopData> stopDataList = SegmentToStopDataFromConverter.convert(segments);
		final String dataPart = "data=";
		if (directionsUrl.toString().contains(dataPart)) {
			final String dataStr = directionsUrl.toString().split(dataPart)[1].split("&")[0];
			final String delimiter = "!";
			final String[] tokens =
					dataStr.startsWith(delimiter) ?
							dataStr.substring(delimiter.length()).split(delimiter) :
							dataStr.split(delimiter);
			int waypointIdx = 0;
			int i = 0;
			while (i < tokens.length) {
				final String token = tokens[i++];
				final String messageMarker = "1m";
				if (token.startsWith(messageMarker) && token.length() == 3) {
					final char subTokenCountChar = token.charAt(messageMarker.length());
					if (subTokenCountChar == '0' || subTokenCountChar == '2' || subTokenCountChar == '5') {
						final int subTokenCount = Character.getNumericValue(subTokenCountChar);
						final int windowEnd = i + subTokenCount;
						if (waypointIdx < stopDataList.size()) {
							final StopData stopData = stopDataList.get(waypointIdx);
							while (i < windowEnd && i < tokens.length) {
								final String subToken = tokens[i++];
								assignParsedTokenToStopData(subToken, stopData);
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

	private static void assignParsedTokenToStopData(final String token, final StopData stopData) {
		final Parser<String> placeIdParser = new PlaceIdParser();
		final Parser<Double> latitudeParser = Parsers.createLatitudeParser();
		final Parser<Double> longitudeParser = Parsers.createLongitudeParser();
		if (placeIdParser.matches(token)) {
			stopData.placeId = Optional.of(placeIdParser.parse(token));
		} else if (latitudeParser.matches(token)) {
			stopData.latitude = Optional.of(latitudeParser.parse(token));
		} else if (longitudeParser.matches(token)) {
			stopData.longitude = Optional.of(longitudeParser.parse(token));
		}
	}

	private static boolean isDirectionsUrl(final URL url) {
		return "https".equals(url.getProtocol()) &&
				"www.google.com".equals(url.getHost()) &&
				url.getPath().startsWith("/maps/dir/");
	}

	private static List<String> getNonEmptySegments(final URL directionsUrl) {
		return SegmentsProvider
				.getSegments(directionsUrl)
				.stream()
				.filter(segment -> !segment.isEmpty())
				.toList();
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

	private static class SegmentToStopDataFromConverter {

		public static List<StopData> convert(final List<String> segments) {
			return Lists
					.asIndexedElements(segments)
					.stream()
					.map(indexedSegment -> convert(indexedSegment.element(), indexedSegment.index() + 1))
					.toList();
		}

		private static StopData convert(final String segment, final int stopNumber) {
			final StopData stopData = new StopData();
			stopData.stopNumber = stopNumber;
			stopData.pathName = URLs.decode(segment);
			if (segment.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+")) {
				final String[] coords = segment.split(",");
				stopData.latitude = Optional.of(Double.parseDouble(coords[0]));
				stopData.longitude = Optional.of(Double.parseDouble(coords[1]));
			}
			return stopData;
		}
	}
}