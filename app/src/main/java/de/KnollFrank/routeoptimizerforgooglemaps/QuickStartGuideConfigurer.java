package de.KnollFrank.routeoptimizerforgooglemaps;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.card.MaterialCardView;

class QuickStartGuideConfigurer {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_SHOW_GUIDE = "show_guide_card";

    private final SharedPreferences sharedPreferences;
    private final MaterialCardView cardGuide;
    private final Button btnShowGuide;
    private final Activity activity;

    public QuickStartGuideConfigurer(final Activity activity) {
        this.activity = activity;
        sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        cardGuide = activity.findViewById(R.id.cardGuide);
        btnShowGuide = activity.findViewById(R.id.btnShowGuide); // Neu deklariert
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
        activity
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
