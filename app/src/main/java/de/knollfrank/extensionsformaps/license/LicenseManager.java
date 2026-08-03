package de.knollfrank.extensionsformaps.license;

import java.util.concurrent.CompletableFuture;

public interface LicenseManager {

    boolean isPro();

    boolean isProFeatureRequired(int currentStopCount);

    CompletableFuture<Boolean> activate(String licenseKey);

    CompletableFuture<Void> verifyExistingLicense();
}
