package de.knollfrank.extensionsformaps.license;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.CompletableFuture;

class LocalLicenseManager implements LicenseManager {

    private static final String KEY_IS_PRO = "is_pro";
    static final String FIXED_LICENSE_KEY = "PRO";

    private final SharedPreferences preferences;

    public LocalLicenseManager(final Context context) {
        this.preferences = context.getSharedPreferences("license_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public boolean isPro() {
        return preferences.getBoolean(KEY_IS_PRO, false);
    }

    @Override
    public boolean isProFeatureRequired(final int currentStopCount) {
        // Free version allows up to 15 stops.
        // If current count is 15, adding another one makes it 16 -> Pro required.
        // If sorting a route with 16 or more stops -> Pro required.
        return currentStopCount > 15;
    }

    @Override
    public CompletableFuture<Boolean> activate(final String licenseKey) {
        final boolean isValid = FIXED_LICENSE_KEY.equals(licenseKey);
        if (isValid) {
            preferences
                    .edit()
                    .putBoolean(KEY_IS_PRO, true)
                    .apply();
        }
        return CompletableFuture.completedFuture(isValid);
    }

    @Override
    public CompletableFuture<Void> verifyExistingLicense() {
        return CompletableFuture.completedFuture(null);
    }
}
