package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.google.common.collect.ImmutableTable;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.osrm.OsrmService;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.osrm.OsrmTableResponse;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OsrmRoutingMatrixProvider implements RoutingMatrixProvider {

    private final OsrmService osrmService;

    public OsrmRoutingMatrixProvider() {
        this(URLs.createUrl("https://router.project-osrm.org/table/v1/driving/"));
    }

    public OsrmRoutingMatrixProvider(final URL baseUrl) {
        osrmService =
                new Retrofit
                        .Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(OsrmService.class);
    }

    @Override
    public RoutingMatrix getRoutingMatrix(final Set<Stop> stops) throws Exception {
        return getRoutingMatrix(stops.stream().toList());
    }

    private RoutingMatrix getRoutingMatrix(final List<Stop> stops) throws IOException {
        final Response<OsrmTableResponse> response =
                osrmService
                        .getTable(
                                format(getGeodetics(stops)),
                                "distance,duration")
                        .execute();
        if (response.isSuccessful() && response.body() != null) {
            final OsrmTableResponse osrmResponse = response.body();
            if ("Ok".equals(osrmResponse.code())) {
                return new RoutingMatrix(getDistanceDurationTable(stops, osrmResponse));
            }
        }
        throw new IOException("Failed to fetch routing matrix from OSRM");
    }

    private static ImmutableTable<Stop, Stop, DistanceDuration> getDistanceDurationTable(
            final List<Stop> stops,
            final OsrmTableResponse response) {
        final ImmutableTable.Builder<Stop, Stop, DistanceDuration> distanceDurationTableBuilder = ImmutableTable.builder();
        final List<List<Double>> distances = response.distances();
        final List<List<Double>> durations = response.durations();
        final int size = distances.size();
        for (int i = 0; i < size; i++) {
            final List<Double> rowDist = distances.get(i);
            final List<Double> rowDur = durations.get(i);
            for (int j = 0; j < size; j++) {
                final Double dist = rowDist.get(j);
                final Double dur = rowDur.get(j);
                distanceDurationTableBuilder.put(
                        stops.get(i),
                        stops.get(j),
                        dist == null || dur == null ?
                                new DistanceDuration(Double.MAX_VALUE, Double.MAX_VALUE) :
                                new DistanceDuration(dist, dur));
            }
        }
        return distanceDurationTableBuilder.build();
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
