package de.knollfrank.extensionsformaps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

import de.knollfrank.extensionsformaps.databinding.ActivityMainBinding;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizerFactory;
import de.knollfrank.extensionsformaps.repository.AccumulatedStopsRepository;

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
            runOnUiThread(() -> {
                if (count > 0) {
                    binding.cardAccumulatedStops.setVisibility(View.VISIBLE);
                    binding.tvAccumulatedTitle.setText(getString(R.string.accumulated_stops_card_title, count));
                    binding.tvAccumulatedDesc.setText(getString(R.string.accumulated_stops_card_desc, count));
                    binding.btnSortAccumulated.setText(getString(R.string.btn_sort_all_stops, count));
                    binding.btnSortAccumulated.setOnClickListener(v ->
                            Executors.newSingleThreadExecutor().execute(() ->
                                    repo.getAccumulatedRoute().ifPresent(route -> runOnUiThread(() -> {
                                        final RouteOptimizationWorkflow workflow =
                                                new RouteOptimizationWorkflow(
                                                        () -> RouteOptimizerFactory.createRouteOptimizer(this),
                                                        this);
                                        workflow.setShowOptimizationTypeDialog(true);
                                        workflow.startOptimizationForRoute(route, this);
                                    }))
                            ));
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
