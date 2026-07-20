package de.KnollFrank.routeoptimizerforgooglemaps;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

import com.google.common.collect.ImmutableList;

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

    private static final String KEY_ADD_STOPS = "ADD_STOPS_ENTRYPOINT_LABEL"; // e.g. "Zwischenstopps hinzufügen"
    private static final String KEY_COUNT_STOPS = "DIRECTIONS_COUNT_STOPS"; // e.g. "%d Haltestellen"
    private static final String SHARE_ID = "com.google.android.apps.maps:id/directions_header_share_action_button";

    private String localizedAddStopsText;
    private String localizedStopsWord;
    private Pattern localizedStopCountPattern;

    private int lastKnownStopCount = 0;
    private boolean isWaitingForShareSheet = false;
    private boolean isWaitingToClickShareAfterBack = false;

    private WindowManager windowManager;
    private View highlightOverlay;
    private final Rect lastOverlayBounds = new Rect();

    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL_MS = 250;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        resolveLocalizedMapsStrings();
        Log.d(TAG, "Service connected and bound! Localized 'Add stops': " + localizedAddStopsText);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeHighlight();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted.");
    }

    // FK-TODO: move method and variables localizedAddStopsText, localizedStopsWord and localizedStopCountPattern to another class
    private void resolveLocalizedMapsStrings() {
        try {
            final Resources mapsRes = getPackageManager().getResourcesForApplication(MAPS_PACKAGE);

            // Resolve "Add stops" label
            // FK-TODO: use OptionalInt?
            final @StringRes int addStopsId = mapsRes.getIdentifier(KEY_ADD_STOPS, "string", MAPS_PACKAGE);
            if (addStopsId != 0) {
                localizedAddStopsText = mapsRes.getString(addStopsId);
            } else {
                localizedAddStopsText = "Add stops"; // Fallback
            }

            // Resolve "n stops" pattern
            // FK-TODO: use OptionalInt?
            final @PluralsRes int countStopsId = mapsRes.getIdentifier(KEY_COUNT_STOPS, "plurals", MAPS_PACKAGE);
            if (countStopsId != 0) {
                // Get the string for a quantity of 5 to extract the pattern (e.g., "%d stops")
                final String patternStr = mapsRes.getQuantityString(countStopsId, 5);
                localizedStopsWord =
                        patternStr
                                .replace("%d", "")
                                .replace("%1$d", "")
                                .trim();
                // Convert "%d stops" to a regex pattern like "(\d+)\s*stops"
                final String regex =
                        patternStr
                                .replace("%d", "(\\d+)")
                                .replace("%1$d", "(\\d+)");
                localizedStopCountPattern = Pattern.compile(regex);
            } else {
                // FK-TODO: replace all fallbacks with Exceptions
                localizedStopsWord = "stops";
                localizedStopCountPattern = Pattern.compile("(\\d+)\\s*(stops|Stopps)"); // Fallback
            }
        } catch (final Exception e) {
            Log.e(TAG, "Failed to resolve Maps strings: " + e.getMessage());
            // FK-TODO: throw exception
            localizedAddStopsText = "Add stops";
            localizedStopsWord = "stops";
            localizedStopCountPattern = Pattern.compile("(\\d+)\\s*(stops|Stopps)");
        }
    }

    private void handleMapsEvent(final AccessibilityEvent event) {
        final long currentTime = System.currentTimeMillis();
        final boolean shouldScan = (currentTime - lastScanTime) > SCAN_INTERVAL_MS;

        // Perform scan only on interval or explicit user action
        if (shouldScan || event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            updateServiceState();
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

    private void updateServiceState() {
        // Optimization: Use native system searches instead of manual Java traversal
        // Priority: Only check the active window to avoid expensive getWindows() call on every event
        final AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }

        // 1. Update Stop Count
        updateStopCount(root);

        // 2. Update Overlay Position
        updateHighlightOverlay(root);

        root.recycle();
    }

    private void updateStopCount(final AccessibilityNodeInfo root) {
        final List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(localizedStopsWord);
        for (final AccessibilityNodeInfo node : nodes) {
            RouteOptimizerAccessibilityService
                    .getTextOrElseGetContentDescription(node)
                    .ifPresent(text -> {
                        final Matcher matcher = localizedStopCountPattern.matcher(text);
                        if (matcher.find()) {
                            try {
                                lastKnownStopCount = Integer.parseInt(matcher.group(1));
                            } catch (final NumberFormatException ignored) {
                            }
                        }
                    });
            node.recycle();
        }
    }

    private void updateHighlightOverlay(AccessibilityNodeInfo root) {
        if (!Settings.canDrawOverlays(this) || lastKnownStopCount < STOP_LIMIT) {
            removeHighlight();
            return;
        }
        this
                .findAddStopsButton(root)
                .ifPresentOrElse(
                        addStopsButton -> {
                            final Rect bounds = new Rect();
                            addStopsButton.getBoundsInScreen(bounds);
                            // Only update UI if bounds have actually changed
                            if (highlightOverlay == null || !lastOverlayBounds.equals(bounds)) {
                                showHighlight(bounds);
                            }
                            addStopsButton.recycle();
                        },
                        this::removeHighlight);
    }

    private Optional<AccessibilityNodeInfo> findAddStopsButton(final AccessibilityNodeInfo root) {
        return root
                .findAccessibilityNodeInfosByText(localizedAddStopsText)
                .stream()
                .findFirst();
    }

    private void showHighlight(final Rect bounds) {
        lastOverlayBounds.set(bounds);
        if (highlightOverlay == null) {
            highlightOverlay = new FrameLayout(this);
            highlightOverlay.setBackgroundResource(R.drawable.border_highlight);
            windowManager.addView(highlightOverlay, getLayoutParams(bounds));
        } else {
            final WindowManager.LayoutParams params = (WindowManager.LayoutParams) highlightOverlay.getLayoutParams();
            updateParams(bounds, params);
            windowManager.updateViewLayout(highlightOverlay, params);
        }
    }

    private void processLimitReached() {
        if (!tryClickShareButton()) {
            Log.d(TAG, "Share button not found. Dismissing overlay via BACK.");
            performGlobalAction(GLOBAL_ACTION_BACK);
            isWaitingToClickShareAfterBack = true;
        }
    }

    private boolean tryClickShareButton() {
        // During automation, we allow scanning all windows as a last resort
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

    // FK-TODO: refactor
    private Optional<AccessibilityNodeInfo> findShareButtonInAllWindows() {
        final List<AccessibilityWindowInfo> windows = getWindows();
        for (final AccessibilityWindowInfo window : windows) {
            final AccessibilityNodeInfo root = window.getRoot();
            if (root != null) {
                final Optional<AccessibilityNodeInfo> button = findShareButton(root);
                if (button.isPresent()) {
                    return button;
                }
                root.recycle();
            }
        }
        return Optional.empty();
    }

    private static Optional<AccessibilityNodeInfo> findShareButton(final AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(SHARE_ID);
        if (nodes.isEmpty()) {
            // FK-TODO: use string resource
            nodes = rootNode.findAccessibilityNodeInfosByText("Share");
        }
        if (nodes.isEmpty()) {
            nodes = rootNode.findAccessibilityNodeInfosByText("Teilen");
        }
        return nodes.isEmpty() ? Optional.empty() : Optional.of(nodes.get(0));
    }

    private boolean isAddStopsText(String text) {
        return text != null && text.contains(localizedAddStopsText);
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

    private static Optional<String> getTextOrElseGetContentDescription(final AccessibilityNodeInfo node) {
        return Optional
                .ofNullable(node.getText())
                .or(() -> Optional.ofNullable(node.getContentDescription()))
                .map(CharSequence::toString);
    }

    private void handleResolverEvent() {
        if (!isWaitingForShareSheet) {
            return;
        }
        final AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }

        final List<AccessibilityNodeInfo> urlNodes =
                ImmutableList
                        .<AccessibilityNodeInfo>builder()
                        .addAll(rootNode.findAccessibilityNodeInfosByViewId("android:id/content_preview_text"))
                        .addAll(rootNode.findAccessibilityNodeInfosByViewId("com.android.intentresolver:id/sem_chooser_sub_title_details_view"))
                        .build();
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
            for (final AccessibilityNodeInfo n : urlNodes) {
                n.recycle();
            }
        }
        rootNode.recycle();
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
            lastOverlayBounds.setEmpty();
        }
    }
}
