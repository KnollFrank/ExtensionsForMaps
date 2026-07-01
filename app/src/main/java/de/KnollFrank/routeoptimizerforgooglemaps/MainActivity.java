package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.OsrmRoutingMatrixProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.OsrmVehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

// FK-TODO: innere Klasse einführen, die statt MainActivity RouteOptimizationOrchestrator.Callback implementiert
public class MainActivity extends AppCompatActivity implements RouteOptimizationOrchestrator.Callback {

    private View progressBar;
    private Button btnOptimize;
    private RouteOptimizationOrchestrator orchestrator;
    private StopsAdapter stopsAdapter;
    // FK-TODO: refactor to Optional<Route> or see com.graphhopper.jsprit.core.problem.job.Service.Builder.setPriority(): default is 2
    private Route currentRoute;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        btnOptimize = findViewById(R.id.btnOptimize);
        stopsAdapter = new StopsAdapter();
        {
            final RecyclerView recyclerViewStops = findViewById(R.id.recyclerViewStops);
            recyclerViewStops.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewStops.setAdapter(stopsAdapter);
        }
        btnOptimize.setOnClickListener(
                view -> {
                    if (currentRoute != null) {
                        final List<Stop> waypointsWithPriorities = stopsAdapter.getStops();
                        final Route routeToOptimize =
                                new Route(
                                        currentRoute.origin(),
                                        waypointsWithPriorities,
                                        currentRoute.destination());
                        orchestrator.optimizeRoute(routeToOptimize);
                    }
                });
        orchestrator =
                new RouteOptimizationOrchestrator(
                        this,
                        new RouteOptimizer(new OsrmVehicleRoutingTransportCostsProvider(new OsrmRoutingMatrixProvider())));
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(final Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction()) && ClipDescription.MIMETYPE_TEXT_PLAIN.equals(intent.getType())) {
            Optional
                    .ofNullable(intent.getStringExtra(Intent.EXTRA_TEXT))
                    .ifPresent(orchestrator::extractRouteFromDirectionsUrl);
        }
    }

    @Override
    public void onExtractRouteFromDirectionsUrlStarted() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            btnOptimize.setVisibility(View.GONE);
        });
    }

    @Override
    public void onExtractRouteFromDirectionsUrlSuccess(final Route route) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            currentRoute = route;
            // FK-TODO: alle Stopps route.stops() der Route anzeigen mit gar keiner vorausgewählten Priority, nämlich OptionalInt.empty(). Für route.origin und route.destination gar keine spinnerPriority zur Verfügung stellen.
            stopsAdapter.setStops(route.waypoints());
            btnOptimize.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onOptimizationStarted() {
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
    }

    @Override
    public void onOptimizationSuccess(final Route optimizedRoute) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            GoogleMapsNavigator.launchRouteOverview(optimizedRoute, this);
            finish();
        });
    }

    @Override
    public void onError(final String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast
                    .makeText(this, message, Toast.LENGTH_LONG)
                    .show();
        });
    }
}