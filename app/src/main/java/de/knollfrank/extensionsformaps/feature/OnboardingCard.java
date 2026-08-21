package de.knollfrank.extensionsformaps.feature;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import de.knollfrank.extensionsformaps.BuildConfig;
import de.knollfrank.extensionsformaps.databinding.ViewOnboardingBinding;

public class OnboardingCard extends MaterialCardView {

    public OnboardingCard(@NonNull final Context context) {
        this(context, null);
    }

    public OnboardingCard(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialCardViewFilledStyle);
    }

    public OnboardingCard(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        ViewOnboardingBinding.inflate(LayoutInflater.from(context), this);
        configureOnboarding();
    }

    private void configureOnboarding() {
        if (!BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            setVisibility(View.VISIBLE);
        }
    }
}
