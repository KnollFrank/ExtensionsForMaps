package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.accessibilityservice.AccessibilityService;

import java.util.Optional;

import de.knollfrank.extensionsformaps.BuildConfig;
import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;

public class ScanAddressFeatureFactory {

    public static Optional<ScanAddressFeature> createScanAddressFeature(
            final AccessibilityService accessibilityService,
            final GoogleAppContext googleAppContext) {
        return BuildConfig.FEATURE_SCAN_ADDRESS_ENABLED ?
                Optional.of(new ScanAddressFeature(accessibilityService, googleAppContext)) :
                Optional.empty();
    }
}
