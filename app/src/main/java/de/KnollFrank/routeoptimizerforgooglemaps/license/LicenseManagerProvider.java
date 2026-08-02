package de.KnollFrank.routeoptimizerforgooglemaps.license;

import android.content.Context;

public class LicenseManagerProvider {

    private static LicenseManager instance;

    public static LicenseManager getInstance(final Context context) {
        if (instance == null) {
            instance = new LocalLicenseManager(context.getApplicationContext());
        }
        return instance;
    }

    public static void setInstance(final LicenseManager licenseManager) {
        instance = licenseManager;
    }
}
