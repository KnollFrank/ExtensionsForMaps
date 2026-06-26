package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity implements RouteOptimizationOrchestrator.Callback {

    private View progressBar;
    private RouteOptimizationOrchestrator orchestrator;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        orchestrator =
                new RouteOptimizationOrchestrator(
                        this,
                        new RouteOptimizer(new OsrmRoutingMatricesProvider()));
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
                    .ifPresent(orchestrator::optimizeRouteOfDirectionsUrl);
        } else {
            finish();
        }
    }

    @Override
    public void onOptimizationStarted() {
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
    }

    @Override
    public void onOptimizationSuccess(final List<RouteOptimizer.Stop> finalRoute) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            GoogleMapsNavigator.launchRouteOverview(this, finalRoute);
            finish();
        });
    }

    @Override
    public void onError(final String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        });
    }
}