package de.knollfrank.extensionsformaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;
import de.knollfrank.extensionsformaps.common.RectWrapper;

class AddStopAutomation {

    private static final String TAG = AddStopAutomation.class.getSimpleName();
    private static final long COOLDOWN_MS = 1000;
    private static final long WATCHDOG_DELAY_MS = 1200;

    private enum State {
        IDLE,
        WAITING_FOR_STOP_COUNT_CLICK,
        WAITING_FOR_LAST_STOP_CLICK,
        WAITING_FOR_CLEAR_CLICK
    }

    private final AccessibilityService accessibilityService;
    private final GoogleMapsContext googleMapsContext;
    private final Handler watchdogHandler = new Handler(Looper.getMainLooper());
    private State state = State.IDLE;
    private long lastActionTime = 0;
    // FK-TODO: make Optional
    private String textToClear = null;

    public AddStopAutomation(final AccessibilityService accessibilityService, final GoogleMapsContext googleMapsContext) {
        this.accessibilityService = accessibilityService;
        this.googleMapsContext = googleMapsContext;
    }

    public void start() {
        Log.d(TAG, "Automation started: WAITING_FOR_STOP_COUNT_CLICK");
        state = State.WAITING_FOR_STOP_COUNT_CLICK;
        lastActionTime = 0;
        textToClear = null;
        scheduleWatchdog();
    }

    public void onStopCountUpdated(final Rect stopCountBounds) {
        if (state == State.WAITING_FOR_STOP_COUNT_CLICK && isCooldownOver()) {
            Log.d(TAG, "Step 1: Clicking stop count label via onStopCountUpdated at " + stopCountBounds);
            if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountBounds)) {
                state = State.WAITING_FOR_LAST_STOP_CLICK;
                markAction();
            }
        }
    }

    public void onGoogleMapsEvent(final AccessibilityNodeInfo root) {
        if (state == State.IDLE) {
            return;
        }
        processState(root);
    }

    private void processState(final AccessibilityNodeInfo root) {
        switch (state) {
            case WAITING_FOR_STOP_COUNT_CLICK -> handleWaitingForStopCountClick(root);
            case WAITING_FOR_LAST_STOP_CLICK -> handleWaitingForLastStopClick(root);
            case WAITING_FOR_CLEAR_CLICK -> handleWaitingForClearClick(root);
        }
    }

    private void handleWaitingForStopCountClick(final AccessibilityNodeInfo root) {
        if (!isCooldownOver()) {
            return;
        }
        findStopCountNode(root).ifPresent(this::performStopCountClick);
    }

    private Optional<AccessibilityNodeInfo> findStopCountNode(final AccessibilityNodeInfo root) {
        return root
                .findAccessibilityNodeInfosByText(googleMapsContext.stopsWord)
                .stream()
                .findFirst();
    }

    private void performStopCountClick(final AccessibilityNodeInfo stopCountNode) {
        Log.d(TAG, "Step 1 (Backup): Found '" + googleMapsContext.stopsWord + "' label. Clicking...");
        if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountNode)) {
            state = State.WAITING_FOR_LAST_STOP_CLICK;
            markAction();
        }
    }

    private void handleWaitingForLastStopClick(final AccessibilityNodeInfo root) {
        final List<AccessibilityNodeInfo> recyclerViews = root.findAccessibilityNodeInfosByViewId("com.google.android.apps.maps:id/edit_waypoints_list");
        if (recyclerViews.isEmpty()) {
            if (isCooldownOver()) {
                // Retry expansion click if the first one was ignored
                this
                        .findStopCountNode(root)
                        .ifPresent(this::performLastStopClick);
            }
            return;
        }

        if (!isCooldownOver()) return;

        final AccessibilityNodeInfo recyclerView = recyclerViews.get(0);
        final int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            final AccessibilityNodeInfo lastChild = recyclerView.getChild(childCount - 1);
            if (lastChild != null) {
                if (listContainsCenterOfItem(recyclerView, lastChild)) {
                    Log.d(TAG, "Step 2: Last waypoint is visible. Clicking...");
                    if (new AccessibilityServiceWrapper(accessibilityService).click(lastChild)) {
                        state = State.WAITING_FOR_CLEAR_CLICK;
                        markAction();
                    }
                } else {
                    Log.d(TAG, "Step 2: Last waypoint is off-screen. Scrolling...");
                    if (recyclerView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                        markAction();
                    }
                }
            }
        }
    }

    private void performLastStopClick(final AccessibilityNodeInfo stopCountNode) {
        Log.d(TAG, "Step 2: Re-clicking expansion label '" + googleMapsContext.stopsWord + "'...");
        if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountNode)) {
            markAction();
        }
    }

    private static boolean listContainsCenterOfItem(final AccessibilityNodeInfo list, final AccessibilityNodeInfo item) {
        return listContainsCenterOfItem(
                new AccessibilityNodeInfoWrapper(list).getBoundsInScreen(),
                new AccessibilityNodeInfoWrapper(item).getBoundsInScreen());
    }

    private static boolean listContainsCenterOfItem(final Rect list, final Rect item) {
        return new RectWrapper(list).contains(new RectWrapper(item).getCenter().orElseThrow());
    }

    private void handleWaitingForClearClick(final AccessibilityNodeInfo root) {
        final List<AccessibilityNodeInfo> editTexts = root.findAccessibilityNodeInfosByViewId("com.google.android.apps.maps:id/search_omnibox_edit_text");
        if (editTexts.isEmpty()) {
            Log.d(TAG, "Step 3: Search bar not found yet. Waiting for transition...");
            return;
        }

        final AccessibilityNodeInfo editText = editTexts.get(0);
        final List<AccessibilityNodeInfo> clearButtons = root.findAccessibilityNodeInfosByViewId("com.google.android.apps.maps:id/search_omnibox_text_clear");

        if (clearButtons.isEmpty()) {
            // Success: EditText is present but the clear button is missing, meaning the field is "empty"
            Log.d(TAG, "Step 3: Clear button is gone. Automation completed successfully!");
            finishAutomation();
            return;
        }

        final String currentText = editText.getText() != null ? editText.getText().toString() : "";
        if (textToClear == null) {
            textToClear = currentText;
            Log.d(TAG, "Step 3: Dummy stop text identified: '" + textToClear + "'");
        }

        if (!currentText.equals(textToClear)) {
            Log.d(TAG, "Step 3: Text has changed or was cleared. Stopping automation.");
            finishAutomation();
            return;
        }
        if (isCooldownOver()) {
            final AccessibilityNodeInfo clearButton = clearButtons.get(0);
            Log.d(TAG, "Step 3: Found clear button. Attempting to click...");
            if (new AccessibilityServiceWrapper(accessibilityService).click(clearButton)) {
                markAction();
            }
        }
    }

    private void scheduleWatchdog() {
        watchdogHandler.removeCallbacksAndMessages(null);
        watchdogHandler.postDelayed(
                () -> {
                    if (state != State.IDLE) {
                        Log.v(TAG, "Watchdog triggered check for state: " + state);
                        new AccessibilityServiceWrapper(accessibilityService)
                                .getRootInActiveWindow()
                                .ifPresent(this::processState);
                        scheduleWatchdog();
                    }
                },
                WATCHDOG_DELAY_MS);
    }

    private boolean isCooldownOver() {
        return System.currentTimeMillis() - lastActionTime >= COOLDOWN_MS;
    }

    private void markAction() {
        lastActionTime = System.currentTimeMillis();
    }

    private void finishAutomation() {
        state = State.IDLE;
        watchdogHandler.removeCallbacksAndMessages(null);
        textToClear = null;
    }

    public void reset() {
        if (state != State.IDLE) {
            Log.d(TAG, "Automation reset from state: " + state);
            finishAutomation();
        }
    }
}
