package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// FK-TODO: refactor
public class RouteOptimizerAccessibilityService extends AccessibilityService {

    private static final String TAG = "RouteOptimizerAS";
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";
    private static final int STOP_LIMIT = 8; // 8 intermediate stops + origin + destination = 10

    private int lastKnownStopCount = 0;
    private boolean isWaitingForShareSheet = false;
    private boolean isWaitingToClickShareAfterBack = false;
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
        // 1. Always update stop count if we can find it in any window
        updateLastKnownStopCountFromWindows();

        // 2. Trigger automation on click
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            final String eventText = getEventText(event);
            if (isAddStopsText(eventText)) {
                if (lastKnownStopCount >= STOP_LIMIT) {
                    Log.d(TAG, "Stop limit reached. Processing automation.");
                    processLimitReached();
                } else {
                    Log.v(TAG, "Clicked Add stops, but count is only " + lastKnownStopCount);
                }
            }
        }

        // 3. If we are waiting for the planning screen to reappear after dismissing an overlay
        if (isWaitingToClickShareAfterBack && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            tryClickShareButton();
        }
    }

    private void processLimitReached() {
        // First, check if share button is already there (maybe no blocking bottom sheet appeared)
        if (!tryClickShareButton()) {
            // Button not found. Assume it's blocked by the "unnecessary" bottom sheet.
            Log.d(TAG, "Share button not found. Dismissing potential overlay via BACK.");
            performGlobalAction(GLOBAL_ACTION_BACK);
            isWaitingToClickShareAfterBack = true;
        }
    }

    private boolean tryClickShareButton() {
        Optional<AccessibilityNodeInfo> shareButton = findShareButtonInAllWindows();
        if (shareButton.isPresent()) {
            shareButton.get().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            isWaitingForShareSheet = true;
            isWaitingToClickShareAfterBack = false;
            Log.d(TAG, "Successfully clicked Share button.");
            return true;
        }
        return false;
    }

    private void updateLastKnownStopCountFromWindows() {
        List<AccessibilityWindowInfo> windows = getWindows();
        for (AccessibilityWindowInfo window : windows) {
            AccessibilityNodeInfo root = window.getRoot();
            if (root == null) continue;

            if (findNodeByText(root, "Add stops", "Stopp hinzufügen") != null) {
                AccessibilityNodeInfo stopCountNode = findStopCountNode(root);
                if (stopCountNode != null) {
                    getTextOrElseGetContentDescription(stopCountNode).ifPresent(text -> {
                        Matcher matcher = stopCountPattern.matcher(text.toString());
                        if (matcher.find()) {
                            try {
                                lastKnownStopCount = Integer.parseInt(matcher.group(1));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    });
                }
            }
            root.recycle();
        }
    }

    private Optional<AccessibilityNodeInfo> findShareButtonInAllWindows() {
        List<AccessibilityWindowInfo> windows = getWindows();
        for (AccessibilityWindowInfo window : windows) {
            AccessibilityNodeInfo root = window.getRoot();
            if (root == null) continue;
            Optional<AccessibilityNodeInfo> button = findShareButton(root);
            if (button.isPresent()) return button;
            root.recycle();
        }
        return Optional.empty();
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
                // FK-TODO: extract constant for "EXTRA_MAPS_URL" and also use in MainActivity
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
