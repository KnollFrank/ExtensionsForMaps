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

import java.net.URL;
import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.feature.UpgradeDialog;
import de.KnollFrank.routeoptimizerforgooglemaps.license.LicenseManager;
import de.KnollFrank.routeoptimizerforgooglemaps.license.LicenseManagerProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteTemplateFactory;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurePlanRoute();
        configureCoffeeButton();
        configurePermissionButtons();
        configureLicenseUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionButtonStates();
        updateLicenseUI();
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
        final View btnCoffee = findViewById(R.id.btnCoffee);
        if (!BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            btnCoffee.setVisibility(View.GONE);
            return;
        }
        btnCoffee.setOnClickListener(
                view ->
                        startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://paypal.me/KnollFrank"))));
    }

    private void configurePermissionButtons() {
        if (!BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            findViewById(R.id.cardPermissions).setVisibility(View.GONE);
            return;
        }
        this
                .findViewById(R.id.btnPermitAccessibility)
                .setOnClickListener(
                        new View.OnClickListener() {

                            @Override
                            public void onClick(final View view) {
                                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            }
                        });
    }

    private void updatePermissionButtonStates() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            updatePermitAccessibilityButtonState();
        }
    }

    private void updatePermitAccessibilityButtonState() {
        final boolean accessibilityServiceEnabled = isAccessibilityServiceEnabled();
        final MaterialButton btnAccessibility = findViewById(R.id.btnPermitAccessibility);
        btnAccessibility.setText(accessibilityServiceEnabled ? R.string.permit_accessibility_done : R.string.permit_accessibility);
    }

    private void configureLicenseUI() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            findViewById(R.id.cardLicense).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.btnActivateLicense).setOnClickListener(v -> UpgradeDialog.showActivationDialog(this, this::updateLicenseUI));
    }

    private void updateLicenseUI() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) return;

        LicenseManager licenseManager = LicenseManagerProvider.getInstance(this);
        boolean isPro = licenseManager.isPro();

        TextView tvStatus = findViewById(R.id.tvLicenseStatus);
        tvStatus.setText(isPro ? R.string.license_status_pro : R.string.license_status_free);

        Button btnActivate = findViewById(R.id.btnActivateLicense);
        btnActivate.setVisibility(isPro ? View.GONE : View.VISIBLE);
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
}
