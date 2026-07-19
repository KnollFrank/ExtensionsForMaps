package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.FrameLayout;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// FK-TODO: refactor
public class RouteOptimizerAccessibilityService extends AccessibilityService {

    private static final String TAG = "RouteOptimizerAS";
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String RESOLVER_PACKAGE = "com.android.intentresolver";
    private static final int STOP_LIMIT = 8; // 8 intermediate stops + origin + destination = 10
    // FK-TODO: es fehlen noch viele Sprachen
    private static final String ADD_STOPS_EN = "Add stops";
    private static final String ADD_STOPS_DE = "Zwischenstopps hinzufügen";
    private static final String STOPS_EN = "stops";
    private static final String STOPS_DE = "Haltestellen";
    private static final String SHARE_ID = "com.google.android.apps.maps:id/directions_header_share_action_button";

    private int lastKnownStopCount = 0;
    private boolean isWaitingForShareSheet = false;
    private boolean isWaitingToClickShareAfterBack = false;
    private final Pattern stopCountPattern =
            Pattern.compile(
                    String.format(
                            "(\\d+)\\s*(%s|%s)",
                            STOPS_EN,
                            STOPS_DE));

    private WindowManager windowManager;
    private View highlightOverlay;
    private final Rect lastOverlayBounds = new Rect();

    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL_MS = 150; // Throttle scans to ~6.6 FPS

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Log.d(TAG, "Service connected and bound!");
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        final CharSequence packageName = event.getPackageName();
        if (packageName == null) {
            return;
        }
        final String pkg = packageName.toString();
        if (MAPS_PACKAGE.equals(pkg)) {
            handleMapsEvent(event);
        } else if (RESOLVER_PACKAGE.equals(pkg)) {
            handleResolverEvent();
        }
    }

    private void handleMapsEvent(final AccessibilityEvent event) {
        final long currentTime = System.currentTimeMillis();
        final boolean shouldScan = (currentTime - lastScanTime) > SCAN_INTERVAL_MS;
        if (shouldScan || event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            performEfficientScan();
            lastScanTime = currentTime;
        }
        // Trigger automation on click
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            final String eventText = getEventText(event);
            if (isAddStopsText(eventText)) {
                if (lastKnownStopCount >= STOP_LIMIT) {
                    Log.d(TAG, "Stop limit reached. Processing automation.");
                    processLimitReached();
                }
            }
        }
        if (isWaitingToClickShareAfterBack && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            tryClickShareButton();
        }
    }

    private static class ScanResult {
        Rect addStopsButtonBounds = null;
        AccessibilityNodeInfo shareButton = null;
        Integer stopCount = null;
    }

    private void performEfficientScan() {
        final ScanResult result = new ScanResult();

        // 1. Try active window first (fastest)
        final AccessibilityNodeInfo activeRoot = getRootInActiveWindow();
        if (activeRoot != null) {
            scanHierarchyPass(activeRoot, result);
        }

        // 2. If not found and we really need it, check other windows
        if (result.addStopsButtonBounds == null || result.stopCount == null) {
            final List<AccessibilityWindowInfo> windows = getWindows();
            for (final AccessibilityWindowInfo window : windows) {
                if (activeRoot != null && window.getId() == activeRoot.getWindowId()) {
                    continue;
                }
                final AccessibilityNodeInfo root = window.getRoot();
                if (root != null) {
                    scanHierarchyPass(root, result);
                }
            }
        }

        // Apply results
        if (result.stopCount != null) {
            lastKnownStopCount = result.stopCount;
        }
        updateHighlightOverlay(result.addStopsButtonBounds);
    }

    private void scanHierarchyPass(final AccessibilityNodeInfo node, final ScanResult result) {
        if (node == null) {
            return;
        }

        // Check for Stop Count
        if (result.stopCount == null) {
            CharSequence text = node.getText();
            if (text == null) {
                text = node.getContentDescription();
            }
            if (text != null) {
                final Matcher matcher = stopCountPattern.matcher(text.toString());
                if (matcher.find()) {
                    result.stopCount = Integer.parseInt(matcher.group(1));
                }
            }
        }

        // Check for Add Stops Button
        if (result.addStopsButtonBounds == null) {
            final CharSequence text = node.getText();
            final CharSequence desc = node.getContentDescription();
            if (isAddStopsText(text) || isAddStopsText(desc)) {
                final Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                result.addStopsButtonBounds = bounds;
            }
        }

        // Check for Share Button (only if we might need to click it soon)
        if (result.shareButton == null) {
            CharSequence nodeText = node.getText();
            if (SHARE_ID.equals(node.getViewIdResourceName()) ||
                    (nodeText != null && (nodeText.toString().equals("Share") || nodeText.toString().equals("Teilen")))) {
                result.shareButton = node;
            }
        }

        // Recursive traversal
        for (int i = 0; i < node.getChildCount(); i++) {
            // Optimization: stop descending if we found everything
            if (result.stopCount != null && result.addStopsButtonBounds != null && result.shareButton != null)
                break;
            scanHierarchyPass(node.getChild(i), result);
        }
    }

    private void updateHighlightOverlay(Rect bounds) {
        if (!Settings.canDrawOverlays(this) || lastKnownStopCount < STOP_LIMIT || bounds == null) {
            removeHighlight();
            return;
        }

        if (highlightOverlay == null) {
            highlightOverlay = new FrameLayout(this);
            highlightOverlay.setBackgroundResource(R.drawable.border_highlight);
            lastOverlayBounds.set(bounds);
            windowManager.addView(highlightOverlay, getLayoutParams(bounds));
        } else if (!lastOverlayBounds.equals(bounds)) {
            // Only update if moved
            lastOverlayBounds.set(bounds);
            final WindowManager.LayoutParams params = (WindowManager.LayoutParams) highlightOverlay.getLayoutParams();
            updateParams(bounds, params);
            windowManager.updateViewLayout(highlightOverlay, params);
        }
    }

    private void processLimitReached() {
        if (!tryClickShareButton()) {
            Log.d(TAG, "Share button not found. Dismissing overlay.");
            performGlobalAction(GLOBAL_ACTION_BACK);
            isWaitingToClickShareAfterBack = true;
        }
    }

    private boolean tryClickShareButton() {
        // Reuse performEfficientScan logic if possible, or targeted search
        final AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return false;
        }

        Optional<AccessibilityNodeInfo> shareButton = findShareButton(root);
        if (shareButton.isPresent()) {
            shareButton.get().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            isWaitingForShareSheet = true;
            isWaitingToClickShareAfterBack = false;
            Log.d(TAG, "Clicked Share button.");
            return true;
        }
        return false;
    }

    private static Optional<AccessibilityNodeInfo> findShareButton(final AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(SHARE_ID);
        if (nodes.isEmpty()) {
            nodes = rootNode.findAccessibilityNodeInfosByText("Share");
        }
        if (nodes.isEmpty()) {
            nodes = rootNode.findAccessibilityNodeInfosByText("Teilen");
        }
        return nodes.isEmpty() ? Optional.empty() : Optional.of(nodes.get(0));
    }

    private boolean isAddStopsText(CharSequence text) {
        if (text == null) {
            return false;
        }
        final String s = text.toString();
        return s.contains(ADD_STOPS_EN) || s.contains(ADD_STOPS_DE);
    }

    private String getEventText(final AccessibilityEvent event) {
        final StringBuilder sb = new StringBuilder();
        if (event.getContentDescription() != null) sb.append(event.getContentDescription());
        for (final CharSequence text : event.getText()) {
            if (text != null) {
                sb.append(text);
            }
        }
        return sb.toString();
    }

    private void handleResolverEvent() {
        if (!isWaitingForShareSheet) {
            return;
        }
        final AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }

        List<AccessibilityNodeInfo> urlNodes = rootNode.findAccessibilityNodeInfosByViewId("android:id/content_preview_text");
        if (urlNodes.isEmpty()) {
            urlNodes = rootNode.findAccessibilityNodeInfosByViewId("com.android.intentresolver:id/sem_chooser_sub_title_details_view");
        }
        if (!urlNodes.isEmpty()) {
            final CharSequence url = urlNodes.get(0).getText();
            if (url != null) {
                Log.d(TAG, "Extracted URL: " + url);
                isWaitingForShareSheet = false;
                performGlobalAction(GLOBAL_ACTION_BACK);
                final Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXTRA_MAPS_URL", url.toString());
                startActivity(intent);
            }
        }
    }

    private static WindowManager.LayoutParams getLayoutParams(final Rect bounds) {
        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        bounds.width(),
                        bounds.height(),
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        updateParams(bounds, params);
        return params;
    }

    private static void updateParams(final Rect src, final WindowManager.LayoutParams dst) {
        dst.x = src.left;
        dst.y = src.top;
        dst.width = src.width();
        dst.height = src.height();
    }

    private void removeHighlight() {
        if (highlightOverlay != null) {
            windowManager.removeView(highlightOverlay);
            highlightOverlay = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeHighlight();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted.");
    }
}
