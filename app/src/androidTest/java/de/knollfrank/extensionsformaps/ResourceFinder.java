package de.knollfrank.extensionsformaps;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import java.util.List;

class ResourceFinder {

    private static final String TAG = ResourceFinder.class.getSimpleName();

    private final String packageName;
    private final Candidates candidates;

    public record Candidates(List<String> names, List<String> values) {
    }

    public ResourceFinder(final String packageName, final Candidates candidates) {
        this.packageName = packageName;
        this.candidates = candidates;
    }

    public void findAndLogResources() {
        Log.d(TAG, "Starting wide-range resource scan for " + packageName + "...");
        try {
            final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            final Resources mapsRes = context.getPackageManager().getResourcesForApplication(packageName);

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
            Log.d(TAG, String.format("Found: [%s] -> [%s]", name, value));
        }
    }

    private boolean isCandidateName(final String name) {
        return contains(name, candidates.names());
    }

    private boolean isCandidateValue(final String value) {
        return contains(value, candidates.values());
    }

    private static boolean contains(final String haystack, final List<String> needles) {
        return needles
                .stream()
                .map(String::toLowerCase)
                .anyMatch(haystack.toLowerCase()::contains);
    }
}
