package de.knollfrank.extensionsformaps;

import android.app.Activity;
import android.content.Context;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;

import java.util.Locale;

import de.knollfrank.extensionsformaps.feature.OptimizationTypeDialog;
import de.knollfrank.extensionsformaps.feature.RoutePreviewDialog;
import de.knollfrank.extensionsformaps.optimize.NativeSuburbResolver;
import de.knollfrank.extensionsformaps.optimize.OptimizationType;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizer;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class RouteOptimizationWorkflow {

    private final RouteOptimizationOrchestrator routeOptimizationOrchestrator;
    private final ProgressOverlay progressOverlay;
    private boolean showOptimizationTypeDialog = false;

    public RouteOptimizationWorkflow(final RouteOptimizer routeOptimizer, final Context context) {
        this.progressOverlay = new ProgressOverlay(context);
        this.routeOptimizationOrchestrator =
                new RouteOptimizationOrchestrator(
                        context,
                        createExtractRouteCallback(context, progressOverlay),
                        createOptimizationCallback(context, progressOverlay),
                        routeOptimizer);
        this.progressOverlay.setOnCancelListener(() -> {
            routeOptimizationOrchestrator.cancelOptimization();
            Toast
                    .makeText(context, R.string.status_sorting_canceled, Toast.LENGTH_SHORT)
                    .show();
            if (context instanceof final Activity activity) {
                activity.finish();
            }
        });
    }

    @VisibleForTesting
    RouteOptimizationWorkflow(final RouteOptimizationOrchestrator routeOptimizationOrchestrator,
                              final ProgressOverlay progressOverlay) {
        this.routeOptimizationOrchestrator = routeOptimizationOrchestrator;
        this.progressOverlay = progressOverlay;
    }

    public void optimizeThenShowRoute(final DirectionsUrl directionsUrl) {
        routeOptimizationOrchestrator.extractRouteFromDirectionsUrl(directionsUrl);
    }

    public void setShowOptimizationTypeDialog(boolean show) {
        this.showOptimizationTypeDialog = show;
    }

    private RouteOptimizationOrchestrator.ExtractRouteCallback createExtractRouteCallback(
            final Context context,
            final ProgressOverlay progressOverlay) {
        return new RouteOptimizationOrchestrator.ExtractRouteCallback() {

            @Override
            public void onExtractRouteFromDirectionsUrlStarted() {
                progressOverlay.show();
                progressOverlay.updateStatus(context.getString(R.string.status_reading_route));
            }

            @Override
            public void onExtractRouteFromDirectionsUrlSuccess(final Route route) {
                final Route enrichedRoute = addSuburbsToAddresses(route, context);
                runOnUiThread(() -> {
                    if (showOptimizationTypeDialog) {
                        showOptimizationTypeDialog(enrichedRoute, context);
                    } else {
                        proceedWithRoute(enrichedRoute, context);
                    }
                });
            }

            @Override
            public void onError(final String message) {
                RouteOptimizationWorkflow.onError(message, progressOverlay, context);
            }

            private void showOptimizationTypeDialog(final Route route, final Context context) {
                OptimizationTypeDialog.show(
                        context,
                        new OptimizationTypeDialog.Callback() {

                            @Override
                            public void onOptimizationTypeSelected(OptimizationType selectedType) {
                                proceedWithRoute(route, context);
                            }

                            @Override
                            public void onCancel() {
                                if (context instanceof final Activity activity) {
                                    activity.finish();
                                }
                            }
                        });
            }

            private void proceedWithRoute(final Route route, final Context context) {
                if (SortConfig.shouldShowRoutePreview(context)) {
                    progressOverlay.hide();
                    RoutePreviewDialog.show(
                            route,
                            SortConfig.getOptimizationType(context),
                            routeOptimizationOrchestrator::optimizeRoute,
                            context);
                } else {
                    routeOptimizationOrchestrator.optimizeRoute(route);
                }
            }

            private Route addSuburbsToAddresses(final Route route, final Context context) {
                final SuburbsToAddressesAdder suburbsToAddressesAdder =
                        new SuburbsToAddressesAdder(
                                new NativeSuburbResolver(
                                        new Geocoder(
                                                context,
                                                Locale.getDefault())),
                                context);
                return suburbsToAddressesAdder.addSuburbsToAddresses(route);
            }
        };
    }

    private static RouteOptimizationOrchestrator.OptimizationCallback createOptimizationCallback(
            final Context context,
            final ProgressOverlay progressOverlay) {
        return new RouteOptimizationOrchestrator.OptimizationCallback() {

            @Override
            public void onOptimizationStarted() {
                progressOverlay.show();
                progressOverlay.updateStatus(context.getString(R.string.status_optimizing_route));
            }

            @Override
            public void onOptimizationProgress(int progressPercentage) {
                progressOverlay.updateProgress(progressPercentage);
            }

            @Override
            public void onOptimizationSuccess(final Route optimizedRoute) {
                progressOverlay.hide();
                GoogleMapsNavigator.launchRouteOverview(optimizedRoute, context);
                if (context instanceof final Activity activity) {
                    activity.finish();
                }
            }

            @Override
            public void onError(final String message) {
                RouteOptimizationWorkflow.onError(message, progressOverlay, context);
            }
        };
    }

    private static void onError(final String message, final ProgressOverlay progressOverlay, final Context context) {
        progressOverlay.hide();
        runOnUiThread(
                () -> {
                    Toast
                            .makeText(context, message, Toast.LENGTH_LONG)
                            .show();
                    if (context instanceof final Activity activity) {
                        activity.finish();
                    }
                });
    }

    // FK-TODO: die Methode runOnUiThread() gibt es mehrfach im Projekt. In eine neue common-Klasse auslagern.
    private static void runOnUiThread(final Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
