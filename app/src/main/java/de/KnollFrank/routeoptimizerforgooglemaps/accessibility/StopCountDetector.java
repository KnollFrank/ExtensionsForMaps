package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class StopCountDetector {

    private static final String TAG = StopCountDetector.class.getSimpleName();

    public interface StopCountListener {

        void onStopCountUpdated(int count, Rect bounds);

        void onStopCountLost();
    }

    private final GoogleMapsContext googleMapsContext;
    private final List<StopCountListener> listeners;

    public StopCountDetector(final GoogleMapsContext googleMapsContext,
                             final List<StopCountListener> listeners) {
        this.googleMapsContext = googleMapsContext;
        this.listeners = listeners;
    }

    public void detect(final AccessibilityNodeInfo root) {
        final Rect bounds = new Rect();
        int count = -1;

        final List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(googleMapsContext.stopsWord());
        for (final AccessibilityNodeInfo node : nodes) {
            final Optional<String> textOpt = getTextOrElseGetContentDescription(node);
            if (textOpt.isPresent()) {
                final String text = textOpt.get();
                final Matcher matcher = googleMapsContext.stopCountPattern().matcher(text);
                if (matcher.find()) {
                    try {
                        count = Integer.parseInt(matcher.group(1));
                        node.getBoundsInScreen(bounds);
                        Log.d(TAG, String.format("Found stop count: '%s' at bounds: %s", text, bounds));
                        break;
                    } catch (final NumberFormatException ignored) {
                    }
                }
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
