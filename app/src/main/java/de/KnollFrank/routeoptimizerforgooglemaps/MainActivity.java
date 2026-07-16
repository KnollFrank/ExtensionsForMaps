package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.billing.BillingListener;
import de.KnollFrank.routeoptimizerforgooglemaps.billing.BillingProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.billing.DebugBillingProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.billing.GooglePlayBillingProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteTemplateFactory;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;

// FK-TODO: refactor
public class MainActivity extends AppCompatActivity implements BillingListener {

    private static final boolean USE_SIMULATION = true; // Toggle for testing

    private BillingProvider billingProvider;
    private MaterialButton btnGenerateTemplate;
    private Slider sliderTotalStops;
    private TextView tvTotalStopsLabel;
    private TextView tvSubscriptionDetails;
    private ColorStateList defaultButtonTint;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        defaultButtonTint = btnGenerateTemplate.getBackgroundTintList();
        billingProvider =
                USE_SIMULATION ?
                        new DebugBillingProvider() :
                        new GooglePlayBillingProvider(this);
        billingProvider.setListener(this);
        billingProvider.startConnection();
        configurePlanRoute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingProvider != null) {
            billingProvider.endConnection();
        }
    }

    @Override
    public void onSubscriptionStatusChanged(final boolean isSubscribed) {
        runOnUiThread(this::updateGenerateTemplateButtonState);
    }

    @Override
    public void onBillingError(final String message) {
        runOnUiThread(() ->
                              Toast
                                      .makeText(this, message, Toast.LENGTH_SHORT)
                                      .show());
    }

    private void initViews() {
        btnGenerateTemplate = findViewById(R.id.btnGenerateTemplate);
        sliderTotalStops = findViewById(R.id.sliderTotalStops);
        tvTotalStopsLabel = findViewById(R.id.tvTotalStopsLabel);
        tvSubscriptionDetails = findViewById(R.id.tvSubscriptionDetails);
    }

    private void configurePlanRoute() {
        tvTotalStopsLabel.setText(getString(R.string.total_stops_label, (int) sliderTotalStops.getValue()));
        sliderTotalStops.addOnChangeListener(
                new Slider.OnChangeListener() {

                    @Override
                    public void onValueChange(@NonNull final Slider slider,
                                              final float value,
                                              final boolean fromUser) {
                        tvTotalStopsLabel.setText(getString(R.string.total_stops_label, (int) value));
                        updateGenerateTemplateButtonState();
                    }
                });
        btnGenerateTemplate.setOnClickListener(view -> onClickGenerateTemplateButton());
        updateGenerateTemplateButtonState();
    }

    private void updateGenerateTemplateButtonState() {
        if (needsPurchase(getSliderTotalStops())) {
            btnGenerateTemplate.setText(R.string.unlock_premium);
            btnGenerateTemplate.setBackgroundTintList(
                    ContextCompat.getColorStateList(
                            this,
                            R.color.color_premium_range));
            tvSubscriptionDetails.setVisibility(View.VISIBLE);
        } else {
            btnGenerateTemplate.setText(R.string.open_in_google_maps);
            btnGenerateTemplate.setBackgroundTintList(defaultButtonTint);
            tvSubscriptionDetails.setVisibility(View.GONE);
        }
    }

    private void onClickGenerateTemplateButton() {
        final int totalStops = getSliderTotalStops();
        if (needsPurchase(totalStops)) {
            billingProvider.launchSubscriptionFlow(this);
        } else {
            GoogleMapsNavigator.launchUrl(
                    createDirectionsUrlTemplate(totalStops),
                    this);
        }
    }

    private int getSliderTotalStops() {
        return (int) sliderTotalStops.getValue();
    }

    private boolean needsPurchase(final int totalStops) {
        return totalStops > 15 && !billingProvider.isSubscribed();
    }

    private URL createDirectionsUrlTemplate(final int totalStops) {
        return RouteToUrlConverter.getUrl(
                RouteTemplateFactory.createRouteTemplate(
                        totalStops));
    }
}
