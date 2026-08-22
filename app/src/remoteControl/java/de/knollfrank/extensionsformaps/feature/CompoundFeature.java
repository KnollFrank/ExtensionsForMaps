package de.knollfrank.extensionsformaps.feature;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class CompoundFeature implements AccessibilityFeature {

    public final List<AccessibilityFeature> features;

    public CompoundFeature(final List<AccessibilityFeature> features) {
        this.features = features;
    }

    @Override
    public void onServiceConnected() {
        features.forEach(AccessibilityFeature::onServiceConnected);
    }

    @Override
    public void onGoogleMapsEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        features.forEach(feature -> feature.onGoogleMapsEvent(event, root));
    }

    @Override
    public void onGoogleAppEvent(final AccessibilityEvent event, final AccessibilityNodeInfo root) {
        features.forEach(feature -> feature.onGoogleAppEvent(event, root));
    }

    @Override
    public void onDestroy() {
        features.forEach(AccessibilityFeature::onDestroy);
    }

    @Override
    public void reset() {
        features.forEach(AccessibilityFeature::reset);
    }
}
