package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.accessibilityservice.AccessibilityService;

import java.util.Optional;

import de.knollfrank.extensionsformaps.BuildConfig;

public class ScanAddressFeatureFactory {

    public static Optional<ScanAddressFeature> createScanAddressFeature(final AccessibilityService accessibilityService) {
        return BuildConfig.FEATURE_SCAN_ADDRESS_ENABLED ?
                Optional.of(new ScanAddressFeature(accessibilityService)) :
                Optional.empty();
    }
}
