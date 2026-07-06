package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.google.common.collect.ImmutableTable;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors.OrsMatrixRequest;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors.OrsMatrixResponse;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors.OrsService;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// FK-TODO: move class to package de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors
// FK-TODO: refactor
public class OpenRouteServiceRoutingMatrixProvider implements RoutingMatrixProvider {

    private final String apiKey;
    private final OrsService orsService;

    public OpenRouteServiceRoutingMatrixProvider(final String apiKey) {
        this(apiKey, URLs.createUrl("https://api.openrouteservice.org/"));
    }

    public OpenRouteServiceRoutingMatrixProvider(final String apiKey, final URL baseUrl) {
        this.apiKey = apiKey;
        // FK-TODO: DRY with OsrmRoutingMatrixProvider
        orsService =
                new Retrofit
                        .Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(OrsService.class);
    }

    @Override
    public RoutingMatrix getRoutingMatrix(final Set<Stop> stops) throws Exception {
        final List<Stop> stopsList = stops.stream().toList();
        final OrsMatrixRequest request =
                new OrsMatrixRequest(
                        stopsList
                                .stream()
                                .map(
                                        stop ->
                                                Arrays.asList(
                                                        stop.geodetic().getLongitude().toDegrees(),
                                                        stop.geodetic().getLatitude().toDegrees()))
                                .toList(),
                        Arrays.asList("distance", "duration"),
                        "m");
        final Response<OrsMatrixResponse> response =
                orsService
                        .getMatrix("driving-car", apiKey, request)
                        .execute();
        if (response.isSuccessful() && response.body() != null) {
            return new RoutingMatrix(getDistanceDurationTable(stopsList, response.body()));
        }
        throw new IOException("Failed to fetch routing matrix from OpenRouteService: " + response.code());
    }

    private static ImmutableTable<Stop, Stop, DistanceDuration> getDistanceDurationTable(final List<Stop> stops, final OrsMatrixResponse response) {
        final ImmutableTable.Builder<Stop, Stop, DistanceDuration> distanceDurationTableBuilder = ImmutableTable.builder();
        final List<List<Double>> distances = response.distances();
        final List<List<Double>> durations = response.durations();
        final int size = stops.size();
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
}
