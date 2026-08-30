package de.knollfrank.extensionsformaps.feature.sort;

import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.VisibleForTesting;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.StopCountDetector;
import de.knollfrank.extensionsformaps.feature.AccessibilityFeature;

public class SortFeature implements AccessibilityFeature, StopCountDetector.StopCountListener {

    private final Buttons buttons;

    SortFeature(final Buttons buttons) {
        this.buttons = buttons;
    }

    @Override
    public void onStopCountUpdated(final int stopCount, final Rect stopCountBounds) {
        buttons.createOrUpdateButtons(stopCountBounds);
    }

    @Override
    public void onStopCountLost() {
        buttons.removeButtons();
    }

    @Override
    public void reset() {
        buttons.removeButtons();
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
    }

    @Override
    public void onDestroy() {
        buttons.removeButtons();
    }

    @VisibleForTesting
    Optional<View> getButtonContainer() {
        return buttons.getButtonContainer();
    }
}
