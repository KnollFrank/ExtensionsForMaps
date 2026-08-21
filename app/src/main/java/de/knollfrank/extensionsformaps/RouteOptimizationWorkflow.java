package de.knollfrank.extensionsformaps;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import de.knollfrank.extensionsformaps.databinding.DialogRoutePreviewBinding;
import de.knollfrank.extensionsformaps.feature.UpgradeDialog;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;
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
                    if (!BuildConfig.SHOW_ACCESSIBILITY_SETTINGS &&
                            LicenseManagerProvider.getInstance(context).isProFeatureRequired(enrichedRoute.stops().size()) &&
                            !LicenseManagerProvider.getInstance(context).isPro()) {
                        progressOverlay.hide();
                        UpgradeDialog.showUpgradeDialog(context, () -> onExtractRouteFromDirectionsUrlSuccess(route));
                        return;
                    }
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
                final AtomicReference<OptimizationType> selectedType = new AtomicReference<>(SortConfig.getOptimizationType(context));
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.sort_dialog_title)
                        .setSingleChoiceItems(
                                new String[]{
                                        context.getString(R.string.settings_type_fixed_destination),
                                        context.getString(R.string.settings_type_any_destination)
                                },
                                selectedType.get() == OptimizationType.FIXED_DESTINATION ? 0 : 1,
                                (dialog, which) ->
                                        selectedType.set(
                                                which == 0 ?
                                                        OptimizationType.FIXED_DESTINATION :
                                                        OptimizationType.ANY_DESTINATION))
                        .setCancelable(false)
                        .setPositiveButton(
                                R.string.ok,
                                (dialog, which) -> {
                                    SortConfig.setOptimizationType(context, selectedType.get());
                                    proceedWithRoute(route, context);
                                })
                        .setNegativeButton(
                                R.string.cancel,
                                (dialog, which) -> {
                                    if (context instanceof final Activity activity) {
                                        activity.finish();
                                    }
                                })
                        .setOnCancelListener(
                                dialog -> {
                                    if (context instanceof final Activity activity) {
                                        activity.finish();
                                    }
                                })
                        .show();
            }

            private void proceedWithRoute(final Route route, final Context context) {
                if (SortConfig.shouldShowRoutePreview(context)) {
                    progressOverlay.hide();
                    showRoutePreviewDialog(route, context);
                } else {
                    routeOptimizationOrchestrator.optimizeRoute(route);
                }
            }

            private void showRoutePreviewDialog(final Route route, final Context context) {
                showRoutePreviewDialog(route, new ContextThemeWrapper(context, R.style.Theme_ExtensionsForMaps_Dialog));
            }

            private void showRoutePreviewDialog(final Route route, final ContextThemeWrapper context) {
                final DialogRoutePreviewBinding binding = DialogRoutePreviewBinding.inflate(LayoutInflater.from(context));
                final StopsAdapter stopsAdapter = new StopsAdapter();
                stopsAdapter.setRoute(route);
                binding.recyclerViewStops.setLayoutManager(new LinearLayoutManager(context));
                binding.recyclerViewStops.setAdapter(stopsAdapter);
                final AlertDialog dialog =
                        new MaterialAlertDialogBuilder(context)
                                .setTitle(R.string.route_preview_title)
                                .setCancelable(false)
                                .setView(binding.getRoot())
                                .setPositiveButton(
                                        R.string.ok,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                stopsAdapter
                                                        .getRoute()
                                                        .ifPresent(routeOptimizationOrchestrator::optimizeRoute);
                                            }
                                        })
                                .setNegativeButton(
                                        R.string.cancel,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                dialog.dismiss();
                                                if (context instanceof final Activity activity) {
                                                    activity.finish();
                                                }
                                            }
                                        })
                                .create();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setType(ProgressOverlay.getWindowType(context));
                }
                dialog.show();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
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
