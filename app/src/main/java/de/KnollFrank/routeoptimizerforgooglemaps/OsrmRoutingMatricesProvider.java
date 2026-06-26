package de.KnollFrank.routeoptimizerforgooglemaps;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class OsrmRoutingMatricesProvider implements RoutingMatricesProvider {

    private static final OkHttpClient httpClient =
            new OkHttpClient
                    .Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

    @Override
    public RouteOptimizer.RoutingMatrices getRoutingMatrices(final double startLat,
                                                             final double startLng,
                                                             final List<RouteOptimizer.Stop> stops) throws Exception {
        final StringBuilder coordinatesStr = new StringBuilder();
        coordinatesStr.append(String.format(Locale.US, "%f,%f", startLng, startLat));
        for (final RouteOptimizer.Stop stop : stops) {
            coordinatesStr.append(";").append(String.format(Locale.US, "%f,%f", stop.lng(), stop.lat()));
        }

        final String url = "https://router.project-osrm.org/table/v1/driving/" + coordinatesStr
                + "?annotations=distance,duration";

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
                    return new RouteOptimizer.RoutingMatrices(distances, durations);
                }
            }
        }
        throw new IllegalStateException();
    }
}
