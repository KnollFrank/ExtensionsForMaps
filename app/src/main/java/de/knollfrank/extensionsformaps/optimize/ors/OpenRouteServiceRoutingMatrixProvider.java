package de.knollfrank.extensionsformaps.optimize.ors;

import com.google.common.collect.ImmutableTable;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.knollfrank.extensionsformaps.common.URLs;
import de.knollfrank.extensionsformaps.optimize.DistanceDuration;
import de.knollfrank.extensionsformaps.optimize.HttpClientProvider;
import de.knollfrank.extensionsformaps.optimize.RoutingMatrix;
import de.knollfrank.extensionsformaps.optimize.RoutingMatrixProvider;
import de.knollfrank.extensionsformaps.route.Stop;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
                        .client(HttpClientProvider.httpClient)
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

    // FK-TODO: DRY with getRoutingMatrix()
    public static void validateApiKey(final String apiKey) throws IOException {
        final OpenRouteServiceRoutingMatrixProvider provider = new OpenRouteServiceRoutingMatrixProvider(apiKey);
        final Response<OrsMatrixResponse> response =
                provider
                        .orsService
                        .getMatrix(
                                "driving-car",
                                apiKey,
                                createDummyRequest())
                        .execute();
        if (!response.isSuccessful()) {
            throw new IOException("API Key validation failed: " + response.code());
        }
    }

    private static OrsMatrixRequest createDummyRequest() {
        return new OrsMatrixRequest(
                Arrays.asList(
                        Arrays.asList(8.681495, 49.41461),
                        Arrays.asList(8.687872, 49.420318)),
                List.of("distance"),
                "m");
    }

    private static ImmutableTable<Stop, Stop, DistanceDuration> getDistanceDurationTable(final List<Stop> stops, final OrsMatrixResponse response) {
        final ImmutableTable.Builder<Stop, Stop, DistanceDuration> distanceDurationTableBuilder = ImmutableTable.builder();
        final List<List<Double>> distances = response.distances();
        final List<List<Double>> durations = response.durations();
        final int size = stops.size();
        for (int row = 0; row < size; row++) {
            final List<Double> rowDist = distances.get(row);
            final List<Double> rowDur = durations.get(row);
            for (int column = 0; column < size; column++) {
                final Double dist = rowDist.get(column);
                final Double dur = rowDur.get(column);
                distanceDurationTableBuilder.put(
                        stops.get(row),
                        stops.get(column),
                        dist == null || dur == null ?
                                new DistanceDuration(Double.MAX_VALUE, Double.MAX_VALUE) :
                                new DistanceDuration(dist, dur));
            }
        }
        return distanceDurationTableBuilder.build();
    }
}
