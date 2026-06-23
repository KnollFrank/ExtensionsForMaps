package de.KnollFrank.routeoptimizerforgooglemaps.route;

import com.google.common.collect.ImmutableList;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class GoogleMapsRouteExtractor {

	private static String[] tokens = new String[]{};
	private static int tokenIndex = 0;

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
			tokens =
					dataStr.startsWith(delimiter) ?
							dataStr.substring(delimiter.length()).split(delimiter) :
							dataStr.split(delimiter);
			int stopDataListIndex = 0;
			reset();
			while (hasTokens()) {
				final String token = nextToken();
				if (ContainerReader.isContainer(token)) {
					final List<String> containerTokens = ContainerReader.readTokensInContainer(token);
					if (List.of(0, 2, 5).contains(containerTokens.size()) && stopDataListIndex < stopDataList.size()) {
						final StopData stopData = stopDataList.get(stopDataListIndex++);
						for (final String containerToken : containerTokens) {
							parseTokenThenAssignToStopData(containerToken, stopData);
						}
					}
				}
			}
		}
		return new Route(StopDataConverter.asStops(stopDataList));
	}

	private static void reset() {
		tokenIndex = 0;
	}

	private static boolean hasTokens() {
		return tokenIndex < tokens.length;
	}

	private static String nextToken() {
		return tokens[tokenIndex++];
	}

	private static class ContainerReader {

		private static final String containerMarker = MarkerFactory.createMarker(1, Datatype.CONTAINER);

		public static boolean isContainer(final String token) {
			return token.startsWith(containerMarker);
		}

		public static List<String> readTokensInContainer(final String token) {
			return getNextTokens(getNumTokensInContainer(token));
		}

		private static int getNumTokensInContainer(final String token) {
			return Character.getNumericValue(token.charAt(containerMarker.length()));
		}

		private static List<String> getNextTokens(final int numTokens) {
			final ImmutableList.Builder<String> nextTokensBuilder = ImmutableList.builder();
			for (int i = 0; i < numTokens; i++) {
				nextTokensBuilder.add(nextToken());
			}
			return nextTokensBuilder.build();
		}
	}

	private static void parseTokenThenAssignToStopData(final String token, final StopData stopData) {
		final Parser<String> placeIdParser = new PlaceIdParser();
		final Parser<Double> latitudeParser = ParserFactory.createLatitudeParser();
		final Parser<Double> longitudeParser = ParserFactory.createLongitudeParser();
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
}