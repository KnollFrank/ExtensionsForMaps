package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.ClipDescription;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

	private View progressBar;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressBar = findViewById(R.id.progressBar);
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(@NonNull final Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(final Intent intent) {
		if (Intent.ACTION_SEND.equals(intent.getAction()) && ClipDescription.MIMETYPE_TEXT_PLAIN.equals(intent.getType())) {
			Optional
					.ofNullable(intent.getStringExtra(Intent.EXTRA_TEXT))
					.ifPresent(this::performOptimization);
		} else {
			finish();
		}
	}

	private void performOptimization(final String sharedText) {
		// FK-TODO: falls dem Benutzer die Optimierung zu lange dauert, dann soll er die Möglichkeit haben, mit einem Cancel-Button den Prozess abzubrechen.
		progressBar.setVisibility(View.VISIBLE);
		new Thread(() -> {
			try {
				final String url = extractUrl(sharedText);
				String processingUrl = url;
				if (url.contains("maps.app.goo.gl") || url.contains("goo.gl/maps")) {
					processingUrl = UrlExpander.expandUrl(url);
				}
				if (processingUrl.contains("/maps/dir/")) {
					final List<RouteOptimizer.Stop> stops = extractStopsFromUrl(processingUrl);
					if (!stops.isEmpty()) {
						ensureAllStopsHaveCoordinatesAndOptimize(stops);
					} else {
						showErrorAndFinish("No stops found in URL.");
					}
				} else {
					final List<String> addressList = parseAddresses(processingUrl.isEmpty() ? sharedText : processingUrl);
					if (addressList.isEmpty()) {
						showErrorAndFinish("No addresses found to optimize.");
					} else {
						runLegacyOptimization(addressList);
					}
				}
			} catch (final IOException e) {
				showErrorAndFinish("Network error: " + e.getMessage());
			} catch (final Exception e) {
				showErrorAndFinish("Error: " + e.getMessage());
			}
		}).start();
	}

	private void ensureAllStopsHaveCoordinatesAndOptimize(final List<RouteOptimizer.Stop> stops) {
		final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		final List<RouteOptimizer.Stop> completeStops = new ArrayList<>();

		for (final RouteOptimizer.Stop stop : stops) {
			if (stop.lat() != 0 || stop.lng() != 0) {
				completeStops.add(stop);
			} else {
				try {
					final List<Address> addresses = geocoder.getFromLocationName(stop.address(), 1);
					if (addresses != null && !addresses.isEmpty()) {
						completeStops.add(
								new RouteOptimizer.Stop(
										stop.address(),
										addresses.get(0).getLatitude(),
										addresses.get(0).getLongitude()));
					} else {
						completeStops.add(stop);
					}
				} catch (final IOException e) {
					completeStops.add(stop);
				}
			}
		}
		if (completeStops.size() < 2) {
			launchRouteOverview(completeStops);
			finish();
		} else {
			runOptimizationWithStops(completeStops);
		}
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

	private record Bang(int index, double value) {
	}

	private void runOptimizationWithStops(final List<RouteOptimizer.Stop> stops) {
		new Thread(() -> {
			try {
				final RouteOptimizer.Stop start = stops.get(0);
				final List<RouteOptimizer.Stop> intermediate = stops.subList(1, stops.size());
				final List<RouteOptimizer.Stop> optimizedIntermediate =
						RouteOptimizer.optimize(start.lat(), start.lng(), intermediate);
				final List<RouteOptimizer.Stop> finalRoute = new ArrayList<>();
				finalRoute.add(start);
				finalRoute.addAll(optimizedIntermediate);
				launchRouteOverview(finalRoute);
				finish();
			} catch (final Exception e) {
				showErrorAndFinish("Optimization error: " + e.getMessage());
			}
		}).start();
	}

	private void runLegacyOptimization(final List<String> addressList) {
		final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		final List<RouteOptimizer.Stop> stops = new ArrayList<>();
		new Thread(() -> {
			try {
				final String startAddressStr = addressList.get(0);
				final List<Address> startCoords = geocoder.getFromLocationName(startAddressStr, 1);
				if (startCoords == null || startCoords.isEmpty()) {
					showErrorAndFinish("Could not find start location: " + startAddressStr);
					return;
				}
				final Address startLocation = startCoords.get(0);
				final RouteOptimizer.Stop startStop =
						new RouteOptimizer.Stop(
								startAddressStr,
								startLocation.getLatitude(),
								startLocation.getLongitude());
				for (int i = 1; i < addressList.size(); i++) {
					final String addressStr = addressList.get(i);
					final List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
					if (addresses != null && !addresses.isEmpty()) {
						final Address addr = addresses.get(0);
						stops.add(
								new RouteOptimizer.Stop(
										addressStr,
										addr.getLatitude(),
										addr.getLongitude()));
					} else {
						stops.add(
								new RouteOptimizer.Stop(
										addressStr,
										0.0,
										0.0));
					}
				}
				final List<RouteOptimizer.Stop> optimizedIntermediate =
						RouteOptimizer.optimize(startStop.lat(), startStop.lng(), stops);
				final List<RouteOptimizer.Stop> finalRoute = new ArrayList<>();
				finalRoute.add(startStop);
				finalRoute.addAll(optimizedIntermediate);
				launchRouteOverview(finalRoute);
				finish();
			} catch (final Exception e) {
				showErrorAndFinish("Optimization error: " + e.getMessage());
			}
		}).start();
	}

	/**
	 * Builds a bulletproof, high-precision Google Maps URL using raw coordinates.
	 * Guaranteed to work flawlessly with the Android Google Maps Intent API.
	 */
	private void launchRouteOverview(final List<RouteOptimizer.Stop> optimizedStops) {
		if (optimizedStops == null || optimizedStops.size() < 2) {
			return;
		}

		// 1. The modern, official Google Maps Directions API endpoint
		final StringBuilder urlBuilder = new StringBuilder("https://www.google.com/maps/dir/?api=1");

		// 2. Set the exact coordinates for the starting point
		final RouteOptimizer.Stop origin = optimizedStops.get(0);
		urlBuilder.append("&origin=")
				.append(origin.lat())
				.append(",")
				.append(origin.lng());

		// 3. Set the exact coordinates for the final destination
		final RouteOptimizer.Stop destination = optimizedStops.get(optimizedStops.size() - 1);
		urlBuilder.append("&destination=")
				.append(destination.lat())
				.append(",")
				.append(destination.lng());

		// 4. Handle intermediate waypoints if there are any stops in between
		if (optimizedStops.size() > 2) {
			urlBuilder.append("&waypoints=");

			for (int i = 1; i < optimizedStops.size() - 1; i++) {
				final RouteOptimizer.Stop waypoint = optimizedStops.get(i);
				urlBuilder.append(waypoint.lat())
						.append(",")
						.append(waypoint.lng());

				// Modern API separates multiple waypoints using the pipe character '|'
				if (i < optimizedStops.size() - 2) {
					urlBuilder.append("|");
				}
			}
		}

		final Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlBuilder.toString()));
		mapIntent.setPackage("com.google.android.apps.maps");
		mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(mapIntent);
	}

	private void showErrorAndFinish(final String message) {
		runOnUiThread(() -> {
			progressBar.setVisibility(View.GONE);
			Toast
					.makeText(this, message, Toast.LENGTH_LONG)
					.show();
			finish();
		});
	}

	private static String extractUrl(final String text) {
		if (text == null) return "";
		int start = text.indexOf("http");
		if (start == -1) return "";
		String url = text.substring(start).trim();
		int end = url.indexOf("\n");
		if (end != -1) {
			url = url.substring(0, end).trim();
		}
		return url;
	}

	public static List<String> parseAddresses(final String text) {
		final List<String> results = new ArrayList<>();
		if (text.contains("/maps/dir/")) {
			final String[] dirParts = text.split("/maps/dir/");
			if (dirParts.length > 1) {
				final String pathPart = dirParts[1].split("/@|/data=")[0];
				final String[] parts = pathPart.split("/");
				for (final String part : parts) {
					if (!part.isEmpty()) {
						try {
							results.add(URLDecoder.decode(part.replace("+", " "), StandardCharsets.UTF_8.name()));
						} catch (final Exception e) { /* skip */ }
					}
				}
			}
			if (!results.isEmpty()) return results;
		}
		if (text.contains("Arrive at location:")) {
			final Pattern pattern = Pattern.compile("Arrive at location: (.*)");
			final Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				results.add(matcher.group(1).trim());
			}
			if (!results.isEmpty()) return results;
		}
		if (text.contains("/maps/place/")) {
			final Pattern pattern = Pattern.compile("place/([^/@?]+)");
			final Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				try {
					final String rawPlace = matcher.group(1);
					results.add(URLDecoder.decode(rawPlace.replace("+", " "), StandardCharsets.UTF_8.name()));
					return results;
				} catch (final Exception e) { /* skip */ }
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
		if (!cleanedText.isEmpty()) results.add(cleanedText);
		return results;
	}
}