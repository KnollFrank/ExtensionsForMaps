package de.knollfrank.extensionsformaps.accessibility;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

// FK-TODO: refactor
public class StopCountDetector {

    private static final String TAG = StopCountDetector.class.getSimpleName();

    public interface StopCountListener {

        void onStopCountUpdated(int stopCount, Rect stopCountBounds);

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
        final Rect stopCountBounds = new Rect();
        int stopCount = -1;

        final List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(googleMapsContext.stopsWord());
        for (final AccessibilityNodeInfo node : nodes) {
            final Optional<String> textOpt = getTextOrElseGetContentDescription(node);
            if (textOpt.isPresent()) {
                final String text = textOpt.get();
                final Matcher matcher = googleMapsContext.stopCountPattern().matcher(text);
                if (matcher.find()) {
                    try {
                        stopCount = Integer.parseInt(matcher.group(1));
                        node.getBoundsInScreen(stopCountBounds);
                        Log.d(TAG, String.format("Found stop count: '%s' at bounds: %s", text, stopCountBounds));
                        break;
                    } catch (final NumberFormatException ignored) {
                    }
                }
            }
        }

        if (stopCount != -1) {
            for (final StopCountListener listener : listeners) {
                listener.onStopCountUpdated(stopCount, stopCountBounds);
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
