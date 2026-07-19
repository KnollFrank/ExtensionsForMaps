package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RouteOptimizerAccessibilityService extends AccessibilityService {

    private static final String TAG = "RouteOptimizerAS";
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";

    private boolean isWaitingForShareSheet = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Service connected and bound!");
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        Log.v(TAG, String.format("Event received: type=%s, package=%s",
                AccessibilityEvent.eventTypeToString(event.getEventType()),
                event.getPackageName()));

        final CharSequence packageName = event.getPackageName();
        if (packageName == null) {
            return;
        }
        if (MAPS_PACKAGE.equals(packageName.toString())) {
            handleMapsEvent(event);
        } else if (RESOLVER_PACKAGE.equals(packageName.toString())) {
            handleResolverEvent();
        }
    }

    private void handleMapsEvent(final AccessibilityEvent event) {
        final AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }
        // Detect "Add stops" click
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            final CharSequence contentDesc = event.getContentDescription();
            if (contentDesc != null && (contentDesc.toString().contains("Add stops") || contentDesc.toString().contains("Stopp hinzufügen"))) {
                if (isAtStopLimit(rootNode)) {
                    Log.d(TAG, "Stop limit reached and 'Add stops' clicked. Triggering automation.");
                    triggerShareFlow(rootNode);
                }
            }
        }
    }

    private boolean isAtStopLimit(final AccessibilityNodeInfo rootNode) {
        // Look for something like "8 stops" (which means 10 total: Start + 8 intermediate + Destination)
        return RouteOptimizerAccessibilityService
                .getStops(rootNode)
                .stream()
                .map(RouteOptimizerAccessibilityService::getTextOrElseGetContentDescription)
                .anyMatch(RouteOptimizerAccessibilityService::contains8);
    }

    private static Optional<CharSequence> getTextOrElseGetContentDescription(final AccessibilityNodeInfo node) {
        return Optional
                .ofNullable(node.getText())
                .or(() -> Optional.ofNullable(node.getContentDescription()));
    }

    private static boolean contains8(final Optional<CharSequence> text) {
        return text
                .map(CharSequence::toString)
                .map(str -> str.contains("8"))
                .orElse(false);
    }

    private static List<AccessibilityNodeInfo> getStops(final AccessibilityNodeInfo rootNode) {
        final List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText("stops");
        return nodes.isEmpty() ?
                rootNode.findAccessibilityNodeInfosByText("Stopps") :
                nodes;
    }

    private void triggerShareFlow(final AccessibilityNodeInfo rootNode) {
        RouteOptimizerAccessibilityService
                .findShareButton(rootNode)
                .ifPresentOrElse(
                        shareButton -> {
                            shareButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            isWaitingForShareSheet = true;
                            Log.d(TAG, "Clicked Share button.");

                        },
                        () -> Log.e(TAG, "Could not find Share button."));
    }

    private static Optional<AccessibilityNodeInfo> findShareButton(final AccessibilityNodeInfo rootNode) {
        return Stream
                .of(
                        rootNode.findAccessibilityNodeInfosByViewId("com.google.android.apps.maps:id/directions_header_share_action_button"),
                        rootNode.findAccessibilityNodeInfosByText("Share"),
                        rootNode.findAccessibilityNodeInfosByText("Teilen"))
                .filter(accessibilityNodeInfos -> !accessibilityNodeInfos.isEmpty())
                .map(accessibilityNodeInfos -> accessibilityNodeInfos.get(0))
                .findFirst();
    }

    private void handleResolverEvent() {
        if (!isWaitingForShareSheet) {
            return;
        }
        final AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }
        final List<AccessibilityNodeInfo> urlNodes = rootNode.findAccessibilityNodeInfosByViewId("android:id/content_preview_text");
        if (!urlNodes.isEmpty()) {
            final AccessibilityNodeInfo urlNode = urlNodes.get(0);
            final CharSequence url = urlNode.getText();
            if (url != null) {
                Log.d(TAG, "Extracted URL: " + url);
                isWaitingForShareSheet = false;

                // 1. Close Share Sheet
                performGlobalAction(GLOBAL_ACTION_BACK);

                // 2. Pass to MainActivity
                final Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXTRA_MAPS_URL", url.toString());
                startActivity(intent);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted.");
    }
}
