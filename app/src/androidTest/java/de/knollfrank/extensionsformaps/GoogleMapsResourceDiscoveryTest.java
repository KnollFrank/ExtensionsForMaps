package de.knollfrank.extensionsformaps;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_MAPS_PACKAGE;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

@RunWith(AndroidJUnit4.class)
public class GoogleMapsResourceDiscoveryTest {

    private static final String TAG = GoogleMapsResourceDiscoveryTest.class.getSimpleName();

    @Test
    public void discoverMapsResources() {
        Log.d(TAG, "Starting wide-range resource scan for Google Maps...");
        try {
            final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            final Resources mapsRes = context.getPackageManager().getResourcesForApplication(GOOGLE_MAPS_PACKAGE);

            // Large scan range to cover strings and plurals in large APKs
            // Package 0x7f, types 0x01-0x20
            for (int type = 0x01; type <= 0x20; type++) {
                final int baseId = 0x7f000000 | (type << 16);
                for (int i = 0; i < 0xFFFF; i++) {
                    final int id = baseId | i;
                    try {
                        final String name = mapsRes.getResourceEntryName(id);
                        final String typeName = mapsRes.getResourceTypeName(id);
                        if ("string".equals(typeName)) {
                            checkAndLog(name, mapsRes.getString(id));
                        } else if ("plurals".equals(typeName)) {
                            checkAndLog(name, mapsRes.getQuantityString(id, 5));
                        }
                    } catch (final Resources.NotFoundException ignored) {
                    }
                }
            }
            Log.d(TAG, "Scan finished.");
        } catch (final Exception e) {
            Log.e(TAG, "Scan failed: " + e.getMessage());
        }
    }

    private void checkAndLog(final String name, final String value) {
        if (isCandidateName(name) || isCandidateValue(value)) {
            Log.d(TAG, String.format("Found: [%s] -> %s", name, value));
        }
    }

    private static boolean isCandidateName(final String name) {
        return Stream
                .of(
                        "stop",
                        "waypoint",
                        "add")
                .map(String::toLowerCase)
                .anyMatch(name.toLowerCase()::contains);
    }

    private static boolean isCandidateValue(final String value) {
        return Stream
                .of(
                        "stopp",
                        "halt",
                        "zwischen",
                        "add stop",
                        "share",
                        "teilen")
                .map(String::toLowerCase)
                .anyMatch(value.toLowerCase()::contains);
    }
}
