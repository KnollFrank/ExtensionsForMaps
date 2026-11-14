package de.knollfrank.extensionsformaps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Optional;
import java.util.concurrent.Executors;

import de.knollfrank.extensionsformaps.databinding.ActivityMainBinding;
import de.knollfrank.extensionsformaps.feature.RouteStagesDialog;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizerFactory;
import de.knollfrank.extensionsformaps.repository.AccumulatedStopsRepository;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteSplitter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configureCoffeeButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccumulatedStopsCard();
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void updateAccumulatedStopsCard() {
        Executors.newSingleThreadExecutor().execute(() -> {
            final AccumulatedStopsRepository repo = new AccumulatedStopsRepository(this);
            final int count = repo.getAccumulatedStopsCount();
            final boolean isSorted = repo.isSorted();
            final Optional<Route> routeOpt = repo.getAccumulatedRoute();

            runOnUiThread(() -> {
                if (count > 0 && routeOpt.isPresent()) {
                    final Route route = routeOpt.get();
                    binding.cardAccumulatedStops.setVisibility(View.VISIBLE);

                    if (isSorted) {
                        final int numStages = RouteSplitter.splitInto10StopBlocks(route).size();
                        binding.tvAccumulatedTitle.setText(getString(R.string.sorted_route_card_title, count, numStages));
                        binding.tvAccumulatedDesc.setText(getString(R.string.sorted_route_card_desc));
                        binding.btnSortAccumulated.setText(getString(R.string.btn_show_stages));
                        binding.btnSortAccumulated.setOnClickListener(v ->
                                RouteStagesDialog.show(this, route, null));
                    } else {
                        binding.tvAccumulatedTitle.setText(getString(R.string.accumulated_stops_card_title, count));
                        binding.tvAccumulatedDesc.setText(getString(R.string.accumulated_stops_card_desc, count));
                        binding.btnSortAccumulated.setText(getString(R.string.btn_sort_all_stops, count));
                        binding.btnSortAccumulated.setOnClickListener(v ->
                                Executors.newSingleThreadExecutor().execute(() ->
                                        repo.getAccumulatedRoute().ifPresent(r -> runOnUiThread(() -> {
                                            final RouteOptimizationWorkflow workflow =
                                                    new RouteOptimizationWorkflow(
                                                            () -> RouteOptimizerFactory.createRouteOptimizer(this),
                                                            this);
                                            workflow.setShowOptimizationTypeDialog(true);
                                            workflow.startOptimizationForRoute(r, this);
                                        }))
                                ));
                    }

                    binding.btnClearAccumulated.setOnClickListener(v ->
                            Executors.newSingleThreadExecutor().execute(() -> {
                                repo.clearAccumulatedStops();
                                runOnUiThread(this::updateAccumulatedStopsCard);
                            }));
                } else {
                    binding.cardAccumulatedStops.setVisibility(View.GONE);
                }
            });
        });
    }

    private void configureCoffeeButton() {
        binding.btnCoffee.setOnClickListener(
                view -> WebUrlLauncher.openUrl(this, "https://paypal.me/KnollFrank"));
    }
}
