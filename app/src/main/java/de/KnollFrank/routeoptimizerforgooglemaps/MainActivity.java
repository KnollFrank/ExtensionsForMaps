package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.billing.BillingHelper;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteTemplateFactory;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;

public class MainActivity extends AppCompatActivity implements BillingHelper.BillingListener {

    private BillingHelper billingHelper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        billingHelper = new BillingHelper(this, this);
        configurePlanRoute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingHelper != null) {
            billingHelper.endConnection();
        }
    }

    private void configurePlanRoute() {
        final Slider sliderTotalStops = findViewById(R.id.sliderTotalStops);
        final TextView tvTotalStopsLabel = findViewById(R.id.tvTotalStopsLabel);
        tvTotalStopsLabel.setText(getString(R.string.total_stops_label, (int) sliderTotalStops.getValue()));
        sliderTotalStops.addOnChangeListener(
                new Slider.OnChangeListener() {

                    @Override
                    public void onValueChange(@NonNull final Slider slider,
                                              final float value,
                                              final boolean fromUser) {
                        tvTotalStopsLabel.setText(getString(R.string.total_stops_label, (int) value));
                    }
                });
        this
                .<Button>findViewById(R.id.btnGenerateTemplate)
                .setOnClickListener(onBtnGenerateTemplateClick(sliderTotalStops, this));

        // Setup "Buy me a coffee" button if it exists in layout
        final View btnCoffee = findViewById(R.id.btnCoffee);
        if (btnCoffee != null) {
            btnCoffee.setOnClickListener(view -> billingHelper.launchBillingFlow(this, BillingHelper.COFFEE_CAPPUCCINO));
        }
    }

    @Override
    public void onDonationSuccessful() {
        runOnUiThread(this::showThankYouDialog);
    }

    @Override
    public void onBillingError(String message) {
        runOnUiThread(
                () ->
                        Toast
                                .makeText(this, message, Toast.LENGTH_SHORT)
                                .show());
    }

    private void showThankYouDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.thanks_title)
                .setMessage(R.string.thanks_message)
                .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private static View.OnClickListener onBtnGenerateTemplateClick(final Slider sliderTotalStops,
                                                                   final Context context) {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                GoogleMapsNavigator.launchUrl(
                        createDirectionsUrlTemplate(getTotalStops()),
                        context);
            }

            private URL createDirectionsUrlTemplate(final int totalStops) {
                return RouteToUrlConverter.getUrl(
                        RouteTemplateFactory.createRouteTemplate(
                                totalStops));
            }

            private int getTotalStops() {
                return (int) sliderTotalStops.getValue();
            }
        };
    }
}
