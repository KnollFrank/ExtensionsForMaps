package de.KnollFrank.routeoptimizerforgooglemaps;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteInputParser {

	private record Bang(int index, double value) {
	}

	// FK-TODO: unit test
	public static String extractUrl(final String text) {
		if (text == null) {
			return "";
		}
		final int start = text.indexOf("http");
		if (start == -1) {
			return "";
		}
		String url = text.substring(start).trim();
		int end = url.indexOf("\n");
		if (end != -1) {
			url = url.substring(0, end).trim();
		}
		return url;
	}

	public static List<RouteOptimizer.Stop> extractStopsFromUrl(final String url) {
		final List<RouteOptimizer.Stop> stops = new ArrayList<>();
		final List<String> labels = new ArrayList<>();
		if (url.contains("/maps/dir/")) {
			final String[] urlParts = url.split("/maps/dir/");
			if (urlParts.length > 1) {
				String pathPart = urlParts[1].split("/@|/data=")[0];
				final String[] parts = pathPart.split("/");
				for (final String part : parts) {
					if (!part.isEmpty()) {
						try {
							labels.add(URLDecoder.decode(part.replace("+", " "), StandardCharsets.UTF_8.name()));
						} catch (final Exception e) { /* skip */ }
					}
				}
			}
		}
		final Pattern pattern = Pattern.compile("!([1-4])d([-0-9.]+)");
		final Matcher matcher = pattern.matcher(url);
		final List<Bang> bangs = new ArrayList<>();
		while (matcher.find()) {
			bangs.add(
					new Bang(
							Integer.parseInt(matcher.group(1)),
							Double.parseDouble(matcher.group(2))));
		}
		int labelIdx = 0;
		int bangIdx = 0;
		while (labelIdx < labels.size()) {
			Double lat = null;
			Double lng = null;
			if (bangIdx < bangs.size() - 1) {
				final Bang first = bangs.get(bangIdx);
				final Bang second = bangs.get(bangIdx + 1);
				if (first.index == 3 && second.index == 4) {
					lat = first.value;
					lng = second.value;
					bangIdx += 2;
				} else if (first.index == 1 && second.index == 2) {
					lng = first.value;
					lat = second.value;
					bangIdx += 2;
				} else {
					bangIdx++;
					continue;
				}
			}
			stops.add(
					new RouteOptimizer.Stop(
							labels.get(labelIdx),
							lat != null ? lat : 0.0,
							lng != null ? lng : 0.0));
			labelIdx++;
		}
		return stops;
	}

	// FK-TODO: refactor
	public static List<String> parseAddresses(final String text) throws UnsupportedEncodingException {
		final List<String> results = new ArrayList<>();
		if (text.contains("/maps/dir/")) {
			final String[] dirParts = text.split("/maps/dir/");
			if (dirParts.length > 1) {
				final String pathPart = dirParts[1].split("/@|/data=")[0];
				final String[] parts = pathPart.split("/");
				for (final String part : parts) {
					if (!part.isEmpty()) {
						results.add(
								URLDecoder.decode(
										part.replace("+", " "),
										StandardCharsets.UTF_8.name()));
					}
				}
			}
			if (!results.isEmpty()) {
				return results;
			}
		}
		if (text.contains("Arrive at location:")) {
			final Pattern pattern = Pattern.compile("Arrive at location: (.*)");
			final Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				results.add(matcher.group(1).trim());
			}
			if (!results.isEmpty()) {
				return results;
			}
		}
		if (text.contains("/maps/place/")) {
			final Pattern pattern = Pattern.compile("place/([^/@?]+)");
			final Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				final String rawPlace = matcher.group(1);
				results.add(
						URLDecoder.decode(
								rawPlace.replace("+", " "),
								StandardCharsets.UTF_8.name()));
				return results;
			}
		}
		final String urlRegex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		final String textWithoutUrl = text.replaceAll(urlRegex, "").trim();
		String cleanedText = textWithoutUrl.replaceAll("\\s*\\n+\\s*", ", ");
		cleanedText = cleanedText.replaceAll(",(\\s*,)+", ",");
		cleanedText = cleanedText.replaceAll("\\s+", " ");
		cleanedText = cleanedText.trim();
		if (cleanedText.startsWith(",")) cleanedText = cleanedText.substring(1).trim();
		if (cleanedText.endsWith(","))
			cleanedText = cleanedText.substring(0, cleanedText.length() - 1).trim();
		if (!cleanedText.isEmpty()) {
			results.add(cleanedText);
		}
		return results;
	}
}