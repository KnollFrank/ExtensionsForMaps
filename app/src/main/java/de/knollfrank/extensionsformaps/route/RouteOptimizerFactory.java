package de.knollfrank.extensionsformaps.route;

import android.content.Context;

import de.knollfrank.extensionsformaps.ApiKeyRepository;
import de.knollfrank.extensionsformaps.SortConfig;
import de.knollfrank.extensionsformaps.optimize.HaversineVehicleRoutingTransportCostsProvider;
import de.knollfrank.extensionsformaps.optimize.OsrmVehicleRoutingTransportCostsProvider;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizer;
import de.knollfrank.extensionsformaps.optimize.VehicleRoutingTransportCostsProvider;
import de.knollfrank.extensionsformaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

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
