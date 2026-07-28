package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContext;
import de.KnollFrank.routeoptimizerforgooglemaps.common.AccessibilityServices;

public class AddStopAutomation {

    private static final String TAG = AddStopAutomation.class.getSimpleName();
    private static final long COOLDOWN_MS = 1000;

    private enum State {
        IDLE,
        WAITING_FOR_STOP_COUNT_CLICK,
        WAITING_FOR_LAST_STOP_CLICK,
        WAITING_FOR_CLEAR_CLICK
    }

    private final AccessibilityService service;
    private final GoogleMapsContext googleMapsContext;
    private State state = State.IDLE;
    private long lastActionTime = 0;

    public AddStopAutomation(final AccessibilityService service, final GoogleMapsContext googleMapsContext) {
        this.service = service;
        this.googleMapsContext = googleMapsContext;
    }

    public void start() {
        Log.d(TAG, "Automation started: WAITING_FOR_STOP_COUNT_CLICK");
        state = State.WAITING_FOR_STOP_COUNT_CLICK;
        lastActionTime = 0;
    }

    public void onStopCountUpdated(final Rect stopCountBounds) {
        if (state == State.WAITING_FOR_STOP_COUNT_CLICK && isCooldownOver()) {
            Log.d(TAG, "Step 1: Clicking stop count label via onStopCountUpdated at " + stopCountBounds);
            if (AccessibilityServices.click(service, stopCountBounds)) {
                state = State.WAITING_FOR_LAST_STOP_CLICK;
                markAction();
            }
        }
    }

    public void onGoogleMapsEvent(final AccessibilityNodeInfo root) {
        if (state == State.IDLE || !isCooldownOver()) {
            return;
        }

        switch (state) {
            case WAITING_FOR_STOP_COUNT_CLICK -> handleWaitingForStopCountClick(root);
            case WAITING_FOR_LAST_STOP_CLICK -> handleWaitingForLastStopClick(root);
            case WAITING_FOR_CLEAR_CLICK -> handleWaitingForClearClick(root);
        }
    }

    private void handleWaitingForStopCountClick(final AccessibilityNodeInfo root) {
        final List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(googleMapsContext.stopsWord());
        if (!nodes.isEmpty()) {
            final AccessibilityNodeInfo node = nodes.get(0);
            Log.d(TAG, "Step 1 (Backup): Found '" + googleMapsContext.stopsWord() + "' label. Clicking...");
            if (AccessibilityServices.click(service, node)) {
                state = State.WAITING_FOR_LAST_STOP_CLICK;
                markAction();
            }
            node.recycle();
        }
    }

    private void handleWaitingForLastStopClick(final AccessibilityNodeInfo root) {
        final List<AccessibilityNodeInfo> recyclerViews = root.findAccessibilityNodeInfosByViewId("com.google.android.apps.maps:id/edit_waypoints_list");
        if (recyclerViews.isEmpty()) {
            Log.d(TAG, "Step 2: Waypoint list not found yet. Waiting...");
            return;
        }

        final AccessibilityNodeInfo recyclerView = recyclerViews.get(0);
        final int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            final AccessibilityNodeInfo lastChild = recyclerView.getChild(childCount - 1);
            if (lastChild != null) {
                final Rect listBounds = new Rect();
                recyclerView.getBoundsInScreen(listBounds);
                final Rect itemBounds = new Rect();
                lastChild.getBoundsInScreen(itemBounds);

                if (listBounds.contains(itemBounds.centerX(), itemBounds.centerY())) {
                    Log.d(TAG, "Step 2: Last waypoint is visible. Clicking...");
                    if (AccessibilityServices.click(service, lastChild)) {
                        state = State.WAITING_FOR_CLEAR_CLICK;
                        markAction();
                    }
                } else {
                    Log.d(TAG, "Step 2: Last waypoint is off-screen. Scrolling...");
                    if (recyclerView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                        markAction();
                    }
                }
                lastChild.recycle();
            }
        }
        recyclerView.recycle();
    }

    private void handleWaitingForClearClick(final AccessibilityNodeInfo root) {
        final List<AccessibilityNodeInfo> clearButtons = root.findAccessibilityNodeInfosByViewId("com.google.android.apps.maps:id/search_omnibox_text_clear");

        if (clearButtons.isEmpty()) {
            // Success: If the clear button is missing, it means the field has been cleared!
            Log.d(TAG, "Step 3: Clear button is gone. Automation completed successfully!");
            state = State.IDLE;
            return;
        }

        // If the button is still there, try to click it (again)
        final AccessibilityNodeInfo clearButton = clearButtons.get(0);
        Log.d(TAG, "Step 3: Found clear button. Attempting to click...");
        if (AccessibilityServices.click(service, clearButton)) {
            markAction(); // Wait another cooldown period before verifying or retrying
        }
        clearButton.recycle();
    }

    private boolean isCooldownOver() {
        return System.currentTimeMillis() - lastActionTime >= COOLDOWN_MS;
    }

    private void markAction() {
        lastActionTime = System.currentTimeMillis();
    }

    public void reset() {
        if (state != State.IDLE) {
            Log.d(TAG, "Automation reset from state: " + state);
            state = State.IDLE;
            lastActionTime = 0;
        }
    }
}
