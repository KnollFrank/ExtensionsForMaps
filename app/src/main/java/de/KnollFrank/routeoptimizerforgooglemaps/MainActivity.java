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

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_SHOW_GUIDE = "show_guide_card";

    private MaterialCardView cardGuide;
    private ImageButton btnCloseGuide;
    private ImageButton btnHelp;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        {
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
        {
            // FK-TODO: refactor
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            cardGuide = findViewById(R.id.cardGuide);
            btnCloseGuide = findViewById(R.id.btnCloseGuide);
            Button btnShowGuide = findViewById(R.id.btnShowGuide); // Neu deklariert

            // Zustand auslesen (Standard: anzeigen -> true)
            boolean showGuide = sharedPreferences.getBoolean(KEY_SHOW_GUIDE, true);
            updateGuideVisibility(showGuide, cardGuide, btnShowGuide);

            // Anleitung schließen
            btnCloseGuide.setOnClickListener(v -> {
                updateGuideVisibility(false, cardGuide, btnShowGuide);
                sharedPreferences.edit().putBoolean(KEY_SHOW_GUIDE, false).apply();
            });

            // Anleitung wieder anzeigen
            btnShowGuide.setOnClickListener(v -> {
                updateGuideVisibility(true, cardGuide, btnShowGuide);
                sharedPreferences.edit().putBoolean(KEY_SHOW_GUIDE, true).apply();
            });
        }
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

    // FK-TODO: refactor
    // Kleine Hilfsmethode für den sauberen Wechsel
    private void updateGuideVisibility(boolean show, View card, View button) {
        if (show) {
            card.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        } else {
            card.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }
}