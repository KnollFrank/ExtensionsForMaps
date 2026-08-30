package de.knollfrank.extensionsformaps.feature;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public interface AccessibilityFeature {

    void onServiceConnected();

    void onGoogleMapsEvent(AccessibilityEvent event, AccessibilityNodeInfo root);

    void onGoogleAppEvent(AccessibilityEvent event, AccessibilityNodeInfo root);

    void onDestroy();

    void reset();
}
