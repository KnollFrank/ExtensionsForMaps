package de.knollfrank.extensionsformaps.optimize;

import android.content.Context;

import de.knollfrank.extensionsformaps.ApiKeyRepository;
import de.knollfrank.extensionsformaps.SortConfig;
import de.knollfrank.extensionsformaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

public class RouteOptimizerFactory {

    public static RouteOptimizer createRouteOptimizer(final Context context) {
        return new RouteOptimizer(getVehicleRoutingTransportCostsProvider(context));
    }

    private static VehicleRoutingTransportCostsProvider getVehicleRoutingTransportCostsProvider(final Context context) {
        if (SortConfig.getOptimizationMethod(context) == SortConfig.OptimizationMethod.HAVERSINE) {
            return new HaversineVehicleRoutingTransportCostsProvider();
        } else {
            return new OsrmVehicleRoutingTransportCostsProvider(
                    new OpenRouteServiceRoutingMatrixProvider(
                            new ApiKeyRepository(context).getApiKey().orElseThrow()));
        }
    }
}
