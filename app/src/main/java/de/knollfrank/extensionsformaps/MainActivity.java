package de.knollfrank.extensionsformaps;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.util.List;

import de.knollfrank.extensionsformaps.common.Runnables;
import de.knollfrank.extensionsformaps.databinding.ActivityMainBinding;
import de.knollfrank.extensionsformaps.feature.UpgradeDialog;
import de.knollfrank.extensionsformaps.license.LicenseManager;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;
import de.knollfrank.extensionsformaps.route.RouteDirectionsUrlConverter;
import de.knollfrank.extensionsformaps.route.RouteTemplateFactory;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configurePlanRoute();
        configureOnboarding();
        configureCoffeeButton();
        configurePermissionButtons();
        configureLicenseUI();
        verifyLicenseInBackground();
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
        if (!BuildConfig.SHOW_PLANNING_CARD) {
            binding.cardPlanning.setVisibility(View.GONE);
            return;
        }
        binding.tvTotalStopsLabel.setText(getString(R.string.total_stops_label, (int) binding.sliderTotalStops.getValue()));
        binding.sliderTotalStops.addOnChangeListener(
                new Slider.OnChangeListener() {

                    @Override
                    public void onValueChange(@NonNull final Slider slider,
                                              final float value,
                                              final boolean fromUser) {
                        binding.tvTotalStopsLabel.setText(getString(R.string.total_stops_label, (int) value));
                    }
                });
        binding.btnGenerateTemplate.setOnClickListener(onBtnGenerateTemplateClick(binding.sliderTotalStops, this));
    }

    private void configureOnboarding() {
        if (!BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            binding.cardOnboarding.setVisibility(View.VISIBLE);
        }
    }

    private void configureCoffeeButton() {
        if (!BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            binding.btnCoffee.setVisibility(View.GONE);
            return;
        }
        binding.btnCoffee.setOnClickListener(
                view ->
                        startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://paypal.me/KnollFrank"))));
    }

    private void configurePermissionButtons() {
        if (!BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            binding.cardPermissions.setVisibility(View.GONE);
            return;
        }
        binding.btnPermitAccessibility.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View view) {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                });
    }

    private void configureLicenseUI() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            binding.cardLicense.setVisibility(View.GONE);
            return;
        }
        binding.btnActivateLicense.setOnClickListener(
                view ->
                        UpgradeDialog.showActivationDialog(
                                this,
                                this::updateLicenseUI,
                                Runnables.empty()));
        binding.btnBuyLicense.setOnClickListener(view -> UpgradeDialog.openGumroadCheckout(this));
    }

    private void verifyLicenseInBackground() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) return;
        LicenseManagerProvider
                .getInstance(this)
                .verifyExistingLicense()
                .thenRun(this::updateLicenseUI);
    }

    private void updatePermissionButtonStates() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            updatePermitAccessibilityButtonState();
        }
    }

    private void updatePermitAccessibilityButtonState() {
        final boolean accessibilityServiceEnabled = isAccessibilityServiceEnabled();
        binding.btnPermitAccessibility.setText(accessibilityServiceEnabled ? R.string.permit_accessibility_done : R.string.permit_accessibility);
    }

    private void updateLicenseUI() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) return;

        LicenseManager licenseManager = LicenseManagerProvider.getInstance(this);
        boolean isPro = licenseManager.isPro();

        binding.tvLicenseStatus.setText(isPro ? R.string.license_status_pro : R.string.license_status_free);

        binding.btnActivateLicense.setVisibility(isPro ? View.GONE : View.VISIBLE);
        binding.btnBuyLicense.setVisibility(isPro ? View.GONE : View.VISIBLE);
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
                GoogleMapsNavigator.launchDirectionsUrl(
                        createDirectionsUrlTemplate(getSliderTotalStops()),
                        context);
            }

            private DirectionsUrl createDirectionsUrlTemplate(final int totalStops) {
                return RouteDirectionsUrlConverter.getDirectionsUrl(
                        RouteTemplateFactory.createRouteTemplate(
                                totalStops));
            }

            private int getSliderTotalStops() {
                return (int) sliderTotalStops.getValue();
            }
        };
    }
}
