package de.knollfrank.extensionsformaps.accessibility;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Optionals;

public class StopCountDetector {

    private static final String TAG = StopCountDetector.class.getSimpleName();

    public interface StopCountListener {

        void onStopCountUpdated(int stopCount, Rect stopCountBounds);

        void onStopCountLost();
    }

    private final GoogleMapsContext googleMapsContext;
    private final List<StopCountListener> stopCountListeners;

    public StopCountDetector(final GoogleMapsContext googleMapsContext,
                             final List<StopCountListener> stopCountListeners) {
        this.googleMapsContext = googleMapsContext;
        this.stopCountListeners = stopCountListeners;
    }

    public void detectStopCount(final AccessibilityNodeInfo root) {
        root
                .findAccessibilityNodeInfosByText(googleMapsContext.stopsWord)
                .stream()
                .flatMap(node -> tryDetectStopCount(node).stream())
                .findFirst()
                .ifPresentOrElse(this::notifyStopCountUpdated, this::notifyStopCountLost);
    }

    private Optional<DetectedStopCount> tryDetectStopCount(final AccessibilityNodeInfo node) {
        return StopCountDetector
                .getTextOrContentDescription(node)
                .flatMap(
                        text ->
                                Optionals
                                        .asOptional(googleMapsContext.parseStopCount(text))
                                        .map(count ->
                                                     new DetectedStopCount(
                                                             count,
                                                             new AccessibilityNodeInfoWrapper(node).getBoundsInScreen())));
    }

    private static Optional<String> getTextOrContentDescription(final AccessibilityNodeInfo node) {
        return Optional
                .ofNullable(node.getText())
                .or(() -> Optional.ofNullable(node.getContentDescription()))
                .map(CharSequence::toString);
    }

    private void notifyStopCountUpdated(final DetectedStopCount detectedStopCount) {
        stopCountListeners.forEach(stopCountListener -> stopCountListener.onStopCountUpdated(detectedStopCount.count(), detectedStopCount.bounds()));
    }

    private void notifyStopCountLost() {
        stopCountListeners.forEach(StopCountListener::onStopCountLost);
    }

    private record DetectedStopCount(int count, Rect bounds) {
    }
}
