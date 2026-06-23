package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
		final String dataPartMarker = "data=";
		if (directionsUrl.toString().contains(dataPartMarker)) {
			final TokenIterator tokenIterator = createTokenIterator(directionsUrl, dataPartMarker);
			int stopDataListIndex = 0;
			while (tokenIterator.hasNext()) {
				final String token = tokenIterator.next();
				if (ContainerReader.isContainer(token)) {
					final List<String> containerTokens = ContainerReader.readTokensInContainer(token, tokenIterator);
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

	private static boolean isDirectionsUrl(final URL url) {
		return List.of("http", "https").contains(url.getProtocol()) &&
				url.getHost().contains("google") &&
				url.getPath().startsWith("/maps/dir/");
	}

	private static List<String> getNonEmptySegments(final URL directionsUrl) {
		return SegmentsProvider
				.getSegments(directionsUrl)
				.stream()
				.filter(segment -> !segment.isEmpty())
				.toList();
	}

	private static TokenIterator createTokenIterator(final URL directionsUrl, final String dataPartMarker) {
		return new TokenIterator(
				getTokens(
						getPartFromUrlAfterMarker(
								directionsUrl,
								dataPartMarker)));
	}

	private static String getPartFromUrlAfterMarker(final URL url, final String marker) {
		return url
				.toString()
				.split(marker)[1]
				.split("&")[0];
	}

	private static List<String> getTokens(final String dataPart) {
		final String delimiter = "!";
		return Arrays.asList(
				GoogleMapsRouteExtractor
						.withoutDelimiterAtStart(dataPart, delimiter)
						.split(delimiter));
	}

	private static String withoutDelimiterAtStart(final String str, final String delimiter) {
		return str.startsWith(delimiter) ?
				str.substring(delimiter.length()) :
				str;
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
}