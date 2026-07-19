package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RouteOptimizerAccessibilityService extends AccessibilityService {

    private static final String TAG = "RouteOptimizerAS";
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";
    private static final int STOP_LIMIT = 8; // 8 intermediate stops + origin + destination = 10

    private int lastKnownStopCount = 0;
    private boolean isWaitingForShareSheet = false;
    private final Pattern stopCountPattern = Pattern.compile("(\\d+)\\s*(stops|Stopps)");

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Service connected and bound!");
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
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

        // 1. Update state: Always track stop count if "Add stops" is visible
        updateLastKnownStopCount(rootNode);

        // 2. Trigger automation on click
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            final String eventText = getEventText(event);
            if (isAddStopsText(eventText)) {
                if (lastKnownStopCount >= STOP_LIMIT) {
                    Log.d(TAG, "Stop limit (" + lastKnownStopCount + ") reached. Triggering automation.");
                    triggerShareFlow(rootNode);
                } else {
                    Log.v(TAG, "Clicked Add stops, but count is only " + lastKnownStopCount);
                }
            }
        }

        rootNode.recycle();
    }

    private void updateLastKnownStopCount(AccessibilityNodeInfo rootNode) {
        // Check if "Add stops" button is visible to ensure we are in the route planning screen
        if (findNodeByText(rootNode, "Add stops", "Stopp hinzufügen") != null) {
            // Find the "n stops" label
            AccessibilityNodeInfo stopCountNode = findStopCountNode(rootNode);
            if (stopCountNode != null) {
                String text = getTextOrElseGetContentDescription(stopCountNode).map(CharSequence::toString).orElse("");
                Matcher matcher = stopCountPattern.matcher(text);
                if (matcher.find()) {
                    try {
                        lastKnownStopCount = Integer.parseInt(matcher.group(1));
                        Log.v(TAG, "Updated lastKnownStopCount: " + lastKnownStopCount);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    private AccessibilityNodeInfo findStopCountNode(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText("stops");
        if (nodes.isEmpty()) {
            nodes = rootNode.findAccessibilityNodeInfosByText("Stopps");
        }
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo rootNode, String... texts) {
        for (String text : texts) {
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
            if (!nodes.isEmpty()) return nodes.get(0);
        }
        return null;
    }

    private boolean isAddStopsText(String text) {
        return text.contains("Add stops") || text.contains("Stopp hinzufügen");
    }

    private String getEventText(final AccessibilityEvent event) {
        final StringBuilder sb = new StringBuilder();
        if (event.getContentDescription() != null) {
            sb.append(event.getContentDescription());
        }
        for (final CharSequence text : event.getText()) {
            if (text != null) {
                sb.append(text);
            }
        }
        return sb.toString();
    }

    private static Optional<CharSequence> getTextOrElseGetContentDescription(final AccessibilityNodeInfo node) {
        return Optional
                .ofNullable(node.getText())
                .or(() -> Optional.ofNullable(node.getContentDescription()));
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
                        () -> Log.e(TAG, "Could not find Share button.")
                                );
    }

    private static Optional<AccessibilityNodeInfo> findShareButton(final AccessibilityNodeInfo rootNode) {
        return Stream.of(
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
        rootNode.recycle();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted.");
    }
}
