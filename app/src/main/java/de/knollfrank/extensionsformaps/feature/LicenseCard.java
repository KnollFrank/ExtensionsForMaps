package de.knollfrank.extensionsformaps.feature;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import de.knollfrank.extensionsformaps.BuildConfig;
import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.common.Runnables;
import de.knollfrank.extensionsformaps.databinding.ViewLicenseBinding;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;

public class LicenseCard extends MaterialCardView {

    private ViewLicenseBinding binding;

    public LicenseCard(@NonNull final Context context) {
        this(context, null);
    }

    public LicenseCard(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialCardViewElevatedStyle);
    }

    public LicenseCard(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        binding = ViewLicenseBinding.inflate(LayoutInflater.from(context), this);
        configureLicenseUI();
        verifyLicenseInBackground();
    }

    public void onResume() {
        updateLicenseUI();
    }

    private void configureLicenseUI() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            setVisibility(View.GONE);
            return;
        }
        binding.btnActivateLicense.setOnClickListener(
                view ->
                        UpgradeDialog.showActivationDialog(
                                getContext(),
                                this::updateLicenseUI,
                                Runnables.empty()));
        binding.btnBuyLicense.setOnClickListener(view -> UpgradeDialog.openGumroadCheckout(getContext()));
    }

    private void verifyLicenseInBackground() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            return;
        }
        LicenseManagerProvider
                .getInstance(getContext())
                .verifyExistingLicense()
                .thenRun(this::updateLicenseUI);
    }

    private void updateLicenseUI() {
        if (BuildConfig.SHOW_ACCESSIBILITY_SETTINGS) {
            return;
        }
        final boolean isPro =
                LicenseManagerProvider
                        .getInstance(getContext())
                        .isPro();
        binding.tvLicenseStatus.setText(isPro ? R.string.license_status_pro : R.string.license_status_free);
        binding.btnActivateLicense.setVisibility(isPro ? View.GONE : View.VISIBLE);
        binding.btnBuyLicense.setVisibility(isPro ? View.GONE : View.VISIBLE);
    }
}
