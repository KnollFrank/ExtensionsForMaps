package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.google.common.collect.ImmutableTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OsrmRoutingMatrixProvider implements RoutingMatrixProvider {

    private static final OkHttpClient httpClient =
            new OkHttpClient
                    .Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

    @Override
    public RoutingMatrix getRoutingMatrix(final Set<Stop> stops) throws Exception {
        return getRoutingMatrix(stops.stream().toList());
    }

    // FK-TODO: refactor
    private static RoutingMatrix getRoutingMatrix(final List<Stop> stops) throws JSONException, IOException {
        final URL url = createRequestUrl(stops);
        final Request request = new Request.Builder().url(url).build();
        try (final Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                final JSONObject json = new JSONObject(response.body().string());
                if (json.has("code") && "Ok".equals(json.getString("code"))) {
                    return new RoutingMatrix(getDistanceDurationTable(stops, json));
                }
            }
        }
        throw new IOException("Failed to fetch routing matrix from OSRM");
    }

    private static ImmutableTable<Stop, Stop, DistanceDuration> getDistanceDurationTable(final List<Stop> stops, final JSONObject json) throws JSONException {
        final ImmutableTable.Builder<Stop, Stop, DistanceDuration> distanceDurationTableBuilder = ImmutableTable.builder();
        final JSONArray distancesArray = json.getJSONArray("distances");
        final JSONArray durationsArray = json.getJSONArray("durations");
        final int size = distancesArray.length();
        for (int i = 0; i < size; i++) {
            final JSONArray rowDist = distancesArray.getJSONArray(i);
            final JSONArray rowDur = durationsArray.getJSONArray(i);
            for (int j = 0; j < size; j++) {
                distanceDurationTableBuilder.put(
                        stops.get(i),
                        stops.get(j),
                        rowDist.isNull(j) || rowDur.isNull(j) ?
                                new DistanceDuration(
                                        Double.MAX_VALUE,
                                        Double.MAX_VALUE) :
                                new DistanceDuration(
                                        rowDist.getDouble(j),
                                        rowDur.getDouble(j)));
            }
        }
        return distanceDurationTableBuilder.build();
    }

    private static URL createRequestUrl(final List<Stop> stops) {
        return URLs.createUrl(
                String.format(
                        "https://router.project-osrm.org/table/v1/driving/%s?annotations=distance,duration",
                        format(getGeodetics(stops))));
    }

    private static List<Geodetic> getGeodetics(final List<Stop> stops) {
        return stops
                .stream()
                .map(Stop::geodetic)
                .toList();
    }

    private static String format(final List<Geodetic> geodetics) {
        return geodetics
                .stream()
                .map(OsrmRoutingMatrixProvider::format)
                .collect(Collectors.joining(";"));
    }

    private static String format(final Geodetic geodetic) {
        return String.format(Locale.US, "%f,%f", geodetic.getLongitude().toDegrees(), geodetic.getLatitude().toDegrees());
    }
}
