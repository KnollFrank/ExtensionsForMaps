package de.KnollFrank.routeoptimizerforgooglemaps.route;

import android.content.Context;

import de.KnollFrank.routeoptimizerforgooglemaps.ApiKeyRepository;
import de.KnollFrank.routeoptimizerforgooglemaps.SortConfig;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.HaversineVehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.OsrmVehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.VehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

public class RouteOptimizerFactory {

    public static RouteOptimizer createRouteOptimizer(Context context) {
        return new RouteOptimizer(getVehicleRoutingTransportCostsProvider(context));
    }

    private static VehicleRoutingTransportCostsProvider getVehicleRoutingTransportCostsProvider(Context context) {
        if (SortConfig.getOptimizationMethod(context) == SortConfig.OptimizationMethod.HAVERSINE) {
            return new HaversineVehicleRoutingTransportCostsProvider();
        } else {
            return new OsrmVehicleRoutingTransportCostsProvider(
                    new OpenRouteServiceRoutingMatrixProvider(
                            ApiKeyRepository.getApiKey(context).orElseThrow()));
        }
    }
}
