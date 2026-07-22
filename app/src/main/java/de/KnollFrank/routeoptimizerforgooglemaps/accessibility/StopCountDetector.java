package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StopCountDetector {

    private static final String TAG = "StopCountDetector";

    public interface StopCountListener {

        void onStopCountUpdated(int count, Rect bounds);

        void onStopCountLost();
    }

    private final MapsContext mapsContext;
    private final List<StopCountListener> listeners = new ArrayList<>();

    public StopCountDetector(final MapsContext mapsContext) {
        this.mapsContext = mapsContext;
    }

    public void addListener(final StopCountListener listener) {
        listeners.add(listener);
    }

    public void detect(final AccessibilityNodeInfo root) {
        if (root == null) return;

        final Rect bounds = new Rect();
        int count = -1;

        search:
        for (final String stopsWord : mapsContext.localizedStopsWords()) {
            final List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(stopsWord);
            for (final AccessibilityNodeInfo node : nodes) {
                final Optional<String> textOpt = getTextOrElseGetContentDescription(node);
                if (textOpt.isPresent()) {
                    final String text = textOpt.get();
                    for (final Pattern pattern : mapsContext.localizedStopCountPatterns()) {
                        final Matcher matcher = pattern.matcher(text);
                        if (matcher.find()) {
                            try {
                                count = Integer.parseInt(matcher.group(1));
                                node.getBoundsInScreen(bounds);
                                Log.d(TAG, String.format("Found stop count: '%s' at bounds: %s", text, bounds));
                                node.recycle();
                                break search;
                            } catch (final NumberFormatException ignored) {
                            }
                        }
                    }
                }
                node.recycle();
            }
        }

        if (count != -1) {
            for (final StopCountListener listener : listeners) {
                listener.onStopCountUpdated(count, bounds);
            }
        } else {
            for (final StopCountListener listener : listeners) {
                listener.onStopCountLost();
            }
        }
    }

    private static Optional<String> getTextOrElseGetContentDescription(final AccessibilityNodeInfo node) {
        return Optional
                .ofNullable(node.getText())
                .or(() -> Optional.ofNullable(node.getContentDescription()))
                .map(CharSequence::toString);
    }
}
