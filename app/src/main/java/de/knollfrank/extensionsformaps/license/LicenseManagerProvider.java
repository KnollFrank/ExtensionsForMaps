package de.knollfrank.extensionsformaps.license;

import android.content.Context;

import java.util.Optional;

public class LicenseManagerProvider {

    private static Optional<LicenseManager> instance = Optional.empty();

    public static LicenseManager getInstance(final Context context) {
        if (instance.isEmpty()) {
            instance =
                    Optional.of(
                            GumroadLicenseManagerFactory.createGumroadLicenseManager(
                                    context.getApplicationContext()));
        }
        return instance.orElseThrow();
    }

    public static void setInstance(final LicenseManager licenseManager) {
        instance = Optional.of(licenseManager);
    }
}
