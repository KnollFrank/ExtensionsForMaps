package de.KnollFrank.routeoptimizerforgooglemaps.license;

public interface LicenseManager {

    boolean isPro();

    boolean isProFeatureRequired(int currentStopCount);

    boolean activate(String licenseKey);
}
