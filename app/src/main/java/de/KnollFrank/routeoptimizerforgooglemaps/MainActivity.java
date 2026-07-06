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

import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.OsrmVehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

// FK-TODO: innere Klasse einführen, die statt MainActivity RouteOptimizationOrchestrator.Callback implementiert
public class MainActivity extends AppCompatActivity implements RouteOptimizationOrchestrator.Callback {

    private View progressBar;
    private Button btnOptimize;
    private RouteOptimizationOrchestrator orchestrator;
    private StopsAdapter stopsAdapter;

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
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View view) {
                        stopsAdapter.getRoute().ifPresent(orchestrator::optimizeRoute);
                    }
                });
        orchestrator =
                new RouteOptimizationOrchestrator(
                        this,
                        new RouteOptimizer(
                                new OsrmVehicleRoutingTransportCostsProvider(
                                        new OpenRouteServiceRoutingMatrixProvider("eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjA0NWE4OGQ0NGUzNTQzOGI5YTNjYTNhMzE3ZTIwOTY3IiwiaCI6Im11cm11cjY0In0="))));
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
            stopsAdapter.setRoute(route);
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
            // FK-FIXME: falls optimizedRoute.stops().size() > 10 ist, dann wird die Route in Google Maps abgeschnitten, was eine Eigenschaft der Standard Google Maps Directions URL ist. Verwende stattdessen eine URL mit data-Part (siehe DirectionsUrlTemplateFactory).
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