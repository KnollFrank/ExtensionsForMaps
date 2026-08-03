package de.knollfrank.extensionsformaps.common;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

public class AccessibilityServices {

    private static final String TAG = AccessibilityServices.class.getSimpleName();

    private AccessibilityServices() {
    }

    public static Optional<AccessibilityNodeInfo> getRootInActiveWindow(final AccessibilityService accessibilityService) {
        return Optional.ofNullable(accessibilityService.getRootInActiveWindow());
    }

    public static boolean click(final AccessibilityService service, final AccessibilityNodeInfo node) {
        final Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        return click(service, bounds);
    }

    public static boolean click(final AccessibilityService service, final Rect bounds) {
        if (bounds.isEmpty()) {
            Log.w(TAG, "Attempted to click on an empty Rect. Ignoring.");
            return false;
        }
        final int x = bounds.centerX();
        final int y = bounds.centerY();

        final Path path = new Path();
        path.moveTo(x, y);

        final GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));

        final boolean success = service.dispatchGesture(builder.build(), null, null);
        if (success) {
            Log.d(TAG, String.format("Successfully dispatched click gesture at [%d, %d]", x, y));
        } else {
            Log.e(TAG, String.format("Failed to dispatch click gesture at [%d, %d]. Is 'canPerformGestures' set to true in config?", x, y));
        }
        return success;
    }
}
