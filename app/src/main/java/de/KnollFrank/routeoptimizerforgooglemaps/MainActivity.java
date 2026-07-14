package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;

import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurePlanRoute();
        new QuickStartGuideConfigurer().configureQuickStartGuide();
    }

    private void configurePlanRoute() {
        final Slider sliderTotalStops = findViewById(R.id.sliderTotalStops);
        final TextView tvTotalStopsLabel = findViewById(R.id.tvTotalStopsLabel);
        sliderTotalStops.addOnChangeListener(
                new Slider.OnChangeListener() {

                    @Override
                    public void onValueChange(@NonNull final Slider slider,
                                              final float value,
                                              final boolean fromUser) {
                        tvTotalStopsLabel.setText("Anzahl Stopps: " + (int) value);
                    }
                });
        this
                .<Button>findViewById(R.id.btnGenerateTemplate)
                .setOnClickListener(onBtnGenerateTemplateClick(sliderTotalStops, this));
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
                return DirectionsUrlTemplateFactory.createDirectionsUrlTemplate(
                        // FK-TODO: hier muß der aktuelle GPS-Standort des Benutzers verwendet werden.
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.50248706742132, Unit.DEGREES),
                                new Angle(8.992563508173783, Unit.DEGREES)),
                        totalStops);
            }

            private int getTotalStops() {
                return (int) sliderTotalStops.getValue();
            }
        };
    }

    private class QuickStartGuideConfigurer {

        private static final String PREFS_NAME = "AppPrefs";
        private static final String KEY_SHOW_GUIDE = "show_guide_card";

        private final SharedPreferences sharedPreferences;
        private final MaterialCardView cardGuide;
        private final Button btnShowGuide;

        public QuickStartGuideConfigurer() {
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            cardGuide = findViewById(R.id.cardGuide);
            btnShowGuide = findViewById(R.id.btnShowGuide); // Neu deklariert
        }

        public void configureQuickStartGuide() {
            updateGuideVisibility();
            configureBtnCloseGuide();
            configureBtnShowGuide();
        }

        private void updateGuideVisibility() {
            final boolean showGuide = sharedPreferences.getBoolean(KEY_SHOW_GUIDE, true);
            updateGuideVisibility(showGuide);
        }

        private void configureBtnCloseGuide() {
            MainActivity
                    .this
                    .<ImageButton>findViewById(R.id.btnCloseGuide)
                    .setOnClickListener(
                            view -> updateGuideVisibilityAndPreferences(false));
        }

        private void configureBtnShowGuide() {
            btnShowGuide.setOnClickListener(
                    view -> updateGuideVisibilityAndPreferences(true));
        }

        private void updateGuideVisibilityAndPreferences(final boolean show) {
            updateGuideVisibility(show);
            sharedPreferences
                    .edit()
                    .putBoolean(KEY_SHOW_GUIDE, show)
                    .apply();
        }

        private void updateGuideVisibility(final boolean show) {
            if (show) {
                cardGuide.setVisibility(View.VISIBLE);
                btnShowGuide.setVisibility(View.GONE);
            } else {
                cardGuide.setVisibility(View.GONE);
                btnShowGuide.setVisibility(View.VISIBLE);
            }
        }
    }
}