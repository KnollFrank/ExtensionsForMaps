package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RouteOptimizationOrchestrator {

    public interface Callback {

        void onOptimizationStarted();

        void onOptimizationSuccess(List<RouteOptimizer.Stop> finalRoute);

        void onError(String message);
    }

    private final AddressResolver addressResolver;
    private final Callback callback;

    public RouteOptimizationOrchestrator(final Context context, final Callback callback) {
        // Verwendung des ApplicationContext verhindert Memory Leaks im Hintergrund-Thread
        this.addressResolver = new AddressResolver(context.getApplicationContext());
        this.callback = callback;
    }

    public void processSharedText(final String sharedText) {
        callback.onOptimizationStarted();
        new Thread(() -> {
            try {
                final String url = RouteInputParser.extractUrl(sharedText);
                String processingUrl = url;
                if (url.contains("maps.app.goo.gl") || url.contains("goo.gl/maps")) {
                    processingUrl = UrlExpander.expandUrl(new URL(url)).toString();
                }

                final List<RouteOptimizer.Stop> finalRoute;
                if (processingUrl.contains("/maps/dir/")) {
                    final List<RouteOptimizer.Stop> stops = RouteInputParser.extractStopsFromUrl(processingUrl);
                    if (stops.isEmpty()) {
                        callback.onError("No stops found in URL.");
                        return;
                    }
                    final List<RouteOptimizer.Stop> completeStops = addressResolver.resolveCoordinatesForStops(stops);
                    if (completeStops.size() < 2) {
                        finalRoute = completeStops;
                    } else {
                        finalRoute = optimizeRoute(completeStops);
                    }
                } else {
                    final List<String> addressList = RouteInputParser.parseAddresses(processingUrl.isEmpty() ? sharedText : processingUrl);
                    if (addressList.isEmpty()) {
                        callback.onError("No addresses found to optimize.");
                        return;
                    }
                    final List<RouteOptimizer.Stop> resolvedStops = addressResolver.resolveAddressesToStops(addressList);
                    finalRoute = optimizeRoute(resolvedStops);
                }
                callback.onOptimizationSuccess(finalRoute);
            } catch (final IOException e) {
                callback.onError("Network error: " + e.getMessage());
            } catch (final Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private List<RouteOptimizer.Stop> optimizeRoute(final List<RouteOptimizer.Stop> stops) throws Exception {
        final RouteOptimizer.Stop start = stops.get(0);
        final List<RouteOptimizer.Stop> intermediate = stops.subList(1, stops.size());
        final List<RouteOptimizer.Stop> optimizedIntermediate =
                RouteOptimizer.optimize(
                        start.lat(),
                        start.lng(),
                        intermediate,
                        RouteOptimizer.OptimizationStrategy.OSRM);
        // FK-TODO: use guava ImmutableList
        final List<RouteOptimizer.Stop> finalRoute = new ArrayList<>();
        finalRoute.add(start);
        finalRoute.addAll(optimizedIntermediate);
        return finalRoute;
    }
}