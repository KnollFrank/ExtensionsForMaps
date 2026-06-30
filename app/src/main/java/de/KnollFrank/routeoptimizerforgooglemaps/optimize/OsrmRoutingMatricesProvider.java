package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OsrmRoutingMatricesProvider implements RoutingMatricesProvider {

    private static final OkHttpClient httpClient =
            new OkHttpClient
                    .Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

    @Override
    // FK-TODO: refactor
    public RoutingMatrices getRoutingMatrices(final Stop start, final List<Stop> stops) throws Exception {
        final URL url = createRequestUrl(start, stops);
        final Request request = new Request.Builder().url(url).build();
        try (final Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                final JSONObject json = new JSONObject(response.body().string());
                if (json.has("code") && "Ok".equals(json.getString("code"))) {

                    final JSONArray distancesArray = json.getJSONArray("distances");
                    final JSONArray durationsArray = json.getJSONArray("durations");

                    final int size = distancesArray.length();
                    final double[][] distances = new double[size][size];
                    final double[][] durations = new double[size][size];

                    for (int i = 0; i < size; i++) {
                        final JSONArray rowDist = distancesArray.getJSONArray(i);
                        final JSONArray rowDur = durationsArray.getJSONArray(i);
                        for (int j = 0; j < size; j++) {
                            if (rowDist.isNull(j) || rowDur.isNull(j)) {
                                distances[i][j] = Double.MAX_VALUE;
                                durations[i][j] = Double.MAX_VALUE;
                            } else {
                                distances[i][j] = rowDist.getDouble(j);
                                durations[i][j] = rowDur.getDouble(j);
                            }
                        }
                    }
                    return new RoutingMatrices(distances, durations);
                }
            }
        }
        throw new IllegalStateException();
    }

    private static URL createRequestUrl(final Stop start, final List<Stop> stops) {
        return URLs.createUrl(
                String.format(
                        "https://router.project-osrm.org/table/v1/driving/%s?annotations=distance,duration",
                        format(start.geodetic(), getGeodetics(stops))));
    }

    private static List<Geodetic> getGeodetics(final List<Stop> stops) {
        return stops
                .stream()
                .map(Stop::geodetic)
                .toList();
    }

    private static String format(final Geodetic start, final List<Geodetic> stops) {
        return Lists
                .concat(start, stops)
                .stream()
                .map(OsrmRoutingMatricesProvider::format)
                .collect(Collectors.joining(";"));
    }

    private static String format(final Geodetic geodetic) {
        return String.format(Locale.US, "%f,%f", geodetic.getLongitude().toDegrees(), geodetic.getLatitude().toDegrees());
    }
}
