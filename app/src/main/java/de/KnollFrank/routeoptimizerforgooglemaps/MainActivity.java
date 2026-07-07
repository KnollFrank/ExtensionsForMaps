package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;
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
    // FK-TODO: make Optional<Intent>
    private Intent pendingIntent;
    private static final int REQUEST_CODE = 1001;

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

        this
                .<Button>findViewById(R.id.btnGenerateTemplate)
                .setOnClickListener(
                        new View.OnClickListener() {

                            private final EditText etTotalStops = findViewById(R.id.etTotalStops);

                            @Override
                            public void onClick(final View view) {
                                try {
                                    GoogleMapsNavigator.launchUrl(
                                            createDirectionsUrlTemplate(getTotalStops()),
                                            MainActivity.this);
                                } catch (final Exception e) {
                                    Toast
                                            .makeText(
                                                    MainActivity.this,
                                                    "Fehler beim Generieren des Templates: " + e.getMessage(), Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            private URL createDirectionsUrlTemplate(final int totalStops) throws MalformedURLException {
                                return DirectionsUrlTemplateFactory.createDirectionsUrlTemplate(
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(48.50248706742132, Unit.DEGREES),
                                                new Angle(8.992563508173783, Unit.DEGREES)),
                                        totalStops);
                            }

                            private int getTotalStops() {
                                return Integer.parseInt(etTotalStops.getText().toString());
                            }
                        });
        checkApiKeyAndInit(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        if (orchestrator == null) {
            checkApiKeyAndInit(intent);
        } else {
            handleIntent(intent);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                initOrchestrator(
                        ApiKeyRepository
                                .getApiKey(this)
                                .orElseThrow());
                if (pendingIntent != null) {
                    handleIntent(pendingIntent);
                    pendingIntent = null;
                }
            } else {
                Toast.makeText(this, "API Key wird benötigt!", Toast.LENGTH_LONG).show();
                finish();
            }
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

    private void initOrchestrator(final String apiKey) {
        orchestrator =
                new RouteOptimizationOrchestrator(
                        this,
                        new RouteOptimizer(
                                new OsrmVehicleRoutingTransportCostsProvider(
                                        new OpenRouteServiceRoutingMatrixProvider(apiKey))));
    }

    private void checkApiKeyAndInit(final Intent intent) {
        ApiKeyRepository
                .getApiKey(this)
                .ifPresentOrElse(
                        apiKey -> {
                            initOrchestrator(apiKey);
                            handleIntent(intent);
                        },
                        () -> {
                            pendingIntent = intent;
                            startActivityForResult(
                                    new Intent(this, ApiKeyActivity.class),
                                    REQUEST_CODE);
                        });
    }

    private void handleIntent(final Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction()) && ClipDescription.MIMETYPE_TEXT_PLAIN.equals(intent.getType())) {
            Optional
                    .ofNullable(intent.getStringExtra(Intent.EXTRA_TEXT))
                    .ifPresent(orchestrator::extractRouteFromDirectionsUrl);
        }
    }
}