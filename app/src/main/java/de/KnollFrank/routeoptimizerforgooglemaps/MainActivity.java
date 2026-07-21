package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import de.KnollFrank.routeoptimizerforgooglemaps.route.DirectionsUrlPredicate;
import de.KnollFrank.routeoptimizerforgooglemaps.route.GoogleMapsRouteExtractor;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteTemplateFactory;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Routes;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurePlanRoute();
        configureCoffeeButton();
        configurePermissionButtons();
        handleIntent(Optional.ofNullable(getIntent()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionButtonStates();
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(Optional.of(intent));
    }

    private void handleIntent(final Optional<Intent> intent) {
        ExtraMapsUrlSenderAndReceiver
                .receiveExtraMapsUrl(intent)
                .ifPresent(
                        extraMapsUrl ->
                                // FK-TODO: handle in RouteOptimizerAccessibilityService
                                CompletableFuture
                                        .supplyAsync(() -> addDummyStop(extraMapsUrl))
                                        .thenAccept(url -> runOnUiThread(() -> GoogleMapsNavigator.launchUrl(url, this))));
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
    }

    private void configureCoffeeButton() {
        this
                .findViewById(R.id.btnCoffee)
                .setOnClickListener(
                        view ->
                                startActivity(
                                        new Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://paypal.me/KnollFrank"))));
    }

    private void configurePermissionButtons() {
        this
                .findViewById(R.id.btnPermitAccessibility)
                .setOnClickListener(
                        new View.OnClickListener() {

                            @Override
                            public void onClick(final View view) {
                                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            }
                        });
        this
                .findViewById(R.id.btnPermitOverlay)
                .setOnClickListener(
                        new View.OnClickListener() {

                            @Override
                            public void onClick(final View view) {
                                startActivity(
                                        new Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:" + MainActivity.this.getPackageName())));
                            }
                        });
    }

    private void updatePermissionButtonStates() {
        updatePermitAccessibilityButtonState();
        updatePermitOverlayButtonState();
    }

    private void updatePermitAccessibilityButtonState() {
        final boolean accessibilityServiceEnabled = isAccessibilityServiceEnabled();
        final MaterialButton btnAccessibility = findViewById(R.id.btnPermitAccessibility);
        btnAccessibility.setText(accessibilityServiceEnabled ? R.string.permit_done : R.string.permit_accessibility);
        btnAccessibility.setEnabled(!accessibilityServiceEnabled);
    }

    private void updatePermitOverlayButtonState() {
        final MaterialButton btnOverlay = findViewById(R.id.btnPermitOverlay);
        final boolean canDrawOverlays = Settings.canDrawOverlays(this);
        // FK-TODO: zeige statt dem Text R.string.permit_done bessert so was an wie "✓ Overlays Allowed". Dito btnPermitAccessibility
        btnOverlay.setText(canDrawOverlays ? R.string.permit_done : R.string.permit_overlay);
        btnOverlay.setEnabled(!canDrawOverlays);
    }

    // FK-TODO: refactor and move to another class
    private boolean isAccessibilityServiceEnabled() {
        final AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        final List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (final AccessibilityServiceInfo service : enabledServices) {
            if (service.getResolveInfo().serviceInfo.packageName.equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private static View.OnClickListener onBtnGenerateTemplateClick(final Slider sliderTotalStops,
                                                                   final Context context) {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                GoogleMapsNavigator.launchUrl(
                        createDirectionsUrlTemplate(getSliderTotalStops()),
                        context);
            }

            private URL createDirectionsUrlTemplate(final int totalStops) {
                return RouteToUrlConverter.getUrl(
                        RouteTemplateFactory.createRouteTemplate(
                                totalStops));
            }

            private int getSliderTotalStops() {
                return (int) sliderTotalStops.getValue();
            }
        };
    }

    private static URL expandShortDirectionsUrl(final URL directionsUrl) {
        try {
            return DirectionsUrlPredicate.isShortDirectionsUrl(directionsUrl) ?
                    UrlExpander.expandUrl(directionsUrl) :
                    directionsUrl;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL addDummyStop(final URL directionsUrl) {
        return RouteToUrlConverter.getUrl(
                Routes.addDummyStop(
                        GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(
                                expandShortDirectionsUrl(directionsUrl))));
    }

}
