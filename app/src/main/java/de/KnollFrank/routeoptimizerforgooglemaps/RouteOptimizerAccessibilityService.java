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

import com.google.common.collect.ImmutableList;

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
    // FK-TODO: es fehlen noch viele Sprachen
    private static final String ADD_STOPS_EN = "Add stops";
    private static final String ADD_STOPS_DE = "Zwischenstopps hinzufügen";
    private static final String STOPS_EN = "stops";
    private static final String STOPS_DE = "Haltestellen";

    private int lastKnownStopCount = 0;
    private boolean isWaitingForShareSheet = false;
    private boolean isWaitingToClickShareAfterBack = false;
    private final Pattern stopCountPattern = Pattern.compile(String.format("(\\d+)\\s*(%s|%s)", STOPS_EN, STOPS_DE));

    private WindowManager windowManager;
    private View highlightOverlay;

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
        if (MAPS_PACKAGE.equals(packageName.toString())) {
            handleMapsEvent(event);
        } else if (RESOLVER_PACKAGE.equals(packageName.toString())) {
            handleResolverEvent();
        }
    }

    private void handleMapsEvent(final AccessibilityEvent event) {
        // 1. Update state: Always track stop count if possible
        updateLastKnownStopCountFromWindows();

        // 2. Handle highlight overlay
        updateHighlightOverlay();

        // 3. Handle automation logic
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

        // 4. If we are waiting for the planning screen to reappear after dismissing an overlay
        if (isWaitingToClickShareAfterBack && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            tryClickShareButton();
        }
    }

    private void updateHighlightOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            removeHighlight();
            return;
        }
        if (lastKnownStopCount < STOP_LIMIT) {
            removeHighlight();
            return;
        }
        final AccessibilityNodeInfo addStopsButton = findAddStopsButtonInAllWindows();
        if (addStopsButton != null) {
            final Rect bounds = new Rect();
            addStopsButton.getBoundsInScreen(bounds);
            showHighlight(bounds);
            addStopsButton.recycle();
        } else {
            removeHighlight();
        }
    }

    private void showHighlight(final Rect bounds) {
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

    // FK-TODO: return Optional<AccessibilityNodeInfo>
    private AccessibilityNodeInfo findAddStopsButtonInAllWindows() {
        List<AccessibilityWindowInfo> windows = getWindows();
        for (AccessibilityWindowInfo window : windows) {
            AccessibilityNodeInfo root = window.getRoot();
            if (root == null) continue;
            AccessibilityNodeInfo button = findNodeByText(root, ADD_STOPS_EN, ADD_STOPS_DE);
            if (button != null) return button;
            root.recycle();
        }
        return null;
    }

    private void processLimitReached() {
        if (!tryClickShareButton()) {
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

            if (findNodeByText(root, ADD_STOPS_EN, ADD_STOPS_DE) != null) {
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

    private AccessibilityNodeInfo findStopCountNode(final AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(STOPS_EN);
        if (nodes.isEmpty()) {
            nodes = rootNode.findAccessibilityNodeInfosByText(STOPS_DE);
        }
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    private AccessibilityNodeInfo findNodeByText(final AccessibilityNodeInfo rootNode, String... texts) {
        for (String text : texts) {
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
            if (!nodes.isEmpty()) return nodes.get(0);
        }
        return null;
    }

    private boolean isAddStopsText(String text) {
        return text.contains(ADD_STOPS_EN) || text.contains(ADD_STOPS_DE);
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
        final List<AccessibilityNodeInfo> urlNodes =
                ImmutableList
                        .<AccessibilityNodeInfo>builder()
                        .addAll(rootNode.findAccessibilityNodeInfosByViewId("android:id/content_preview_text"))
                        .addAll(rootNode.findAccessibilityNodeInfosByViewId("com.android.intentresolver:id/sem_chooser_sub_title_details_view"))
                        .build();
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
    public void onDestroy() {
        super.onDestroy();
        removeHighlight();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted.");
    }
}
