package de.KnollFrank.routeoptimizerforgooglemaps;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.net.URL;
import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteTemplateFactory;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> updatePermitNotificationsButtonState());

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurePlanRoute();
        configureCoffeeButton();
        configurePermissionButtons();
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
        this
                .findViewById(R.id.btnPermitNotifications)
                .setOnClickListener(
                        new View.OnClickListener() {

                            @Override
                            public void onClick(final View view) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                        openNotificationSettings();
                                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS)) {
                                        openNotificationSettings();
                                    } else {
                                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                                    }
                                } else {
                                    openNotificationSettings();
                                }
                            }

                            private void openNotificationSettings() {
                                final Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                                startActivity(intent);
                            }
                        });
    }

    private void updatePermissionButtonStates() {
        updatePermitAccessibilityButtonState();
        updatePermitOverlayButtonState();
        updatePermitNotificationsButtonState();
    }

    private void updatePermitAccessibilityButtonState() {
        final boolean accessibilityServiceEnabled = isAccessibilityServiceEnabled();
        final MaterialButton btnAccessibility = findViewById(R.id.btnPermitAccessibility);
        btnAccessibility.setText(accessibilityServiceEnabled ? R.string.permit_accessibility_done : R.string.permit_accessibility);
    }

    private void updatePermitOverlayButtonState() {
        final MaterialButton btnOverlay = findViewById(R.id.btnPermitOverlay);
        final boolean canDrawOverlays = Settings.canDrawOverlays(this);
        btnOverlay.setText(canDrawOverlays ? R.string.permit_overlay_done : R.string.permit_overlay);
    }

    private void updatePermitNotificationsButtonState() {
        final boolean notificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        final MaterialButton btnNotifications = findViewById(R.id.btnPermitNotifications);
        btnNotifications.setText(notificationsEnabled ? R.string.permit_notifications_done : R.string.permit_notifications);
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
