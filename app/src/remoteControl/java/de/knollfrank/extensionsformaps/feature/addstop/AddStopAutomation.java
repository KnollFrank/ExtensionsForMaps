package de.knollfrank.extensionsformaps.feature.addstop;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.util.Pair;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;
import de.knollfrank.extensionsformaps.common.Optionals;
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
    private Optional<String> textToClear = Optional.empty();
    private final Cooldown cooldown = new Cooldown(COOLDOWN_MS);

    public AddStopAutomation(final AccessibilityService accessibilityService, final GoogleMapsContext googleMapsContext) {
        this.accessibilityService = accessibilityService;
        this.googleMapsContext = googleMapsContext;
    }

    public void start() {
        Log.d(TAG, "Automation started: WAITING_FOR_STOP_COUNT_CLICK");
        state = State.WAITING_FOR_STOP_COUNT_CLICK;
        cooldown.resetCooldown();
        textToClear = Optional.empty();
        scheduleWatchdog();
    }

    public void onStopCountUpdated(final Rect stopCountBounds) {
        if (state == State.WAITING_FOR_STOP_COUNT_CLICK && cooldown.isCooldownOver()) {
            Log.d(TAG, "Step 1: Clicking stop count label via onStopCountUpdated at " + stopCountBounds);
            if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountBounds)) {
                state = State.WAITING_FOR_LAST_STOP_CLICK;
                cooldown.startCooldown();
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
            case WAITING_FOR_STOP_COUNT_CLICK ->
                    new WaitingForStopCountClickHandler(cooldown).handleWaitingForStopCountClick(root);
            case WAITING_FOR_LAST_STOP_CLICK ->
                    new WaitingForLastStopClickHandler(cooldown).handleWaitingForLastStopClick(root);
            case WAITING_FOR_CLEAR_CLICK ->
                    new WaitingForClearClickHandler(cooldown).handleWaitingForClearClick(root);
        }
    }

    private class WaitingForStopCountClickHandler {

        private final Cooldown cooldown;

        public WaitingForStopCountClickHandler(final Cooldown cooldown) {
            this.cooldown = cooldown;
        }

        public void handleWaitingForStopCountClick(final AccessibilityNodeInfo root) {
            if (!cooldown.isCooldownOver()) {
                return;
            }
            WaitingForStopCountClickHandler
                    .findStopCountNode(root, googleMapsContext)
                    .ifPresent(this::clickStopCountNode);
        }

        public static Optional<AccessibilityNodeInfo> findStopCountNode(final AccessibilityNodeInfo root,
                                                                        final GoogleMapsContext googleMapsContext1) {
            return new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByText(googleMapsContext1.stopsWord);
        }

        private void clickStopCountNode(final AccessibilityNodeInfo stopCountNode) {
            Log.d(TAG, "Step 1 (Backup): Found '" + googleMapsContext.stopsWord + "' label. Clicking...");
            if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountNode)) {
                state = State.WAITING_FOR_LAST_STOP_CLICK;
                cooldown.startCooldown();
            }
        }
    }

    private class WaitingForLastStopClickHandler {

        private final Cooldown cooldown;

        public WaitingForLastStopClickHandler(final Cooldown cooldown) {
            this.cooldown = cooldown;
        }

        public void handleWaitingForLastStopClick(final AccessibilityNodeInfo root) {
            WaitingForLastStopClickHandler
                    .findEditStopsList(root)
                    .ifPresentOrElse(
                            editStopsList -> {
                                if (!cooldown.isCooldownOver()) {
                                    return;
                                }
                                Optionals.ifPresentBoth(
                                        Pair.create(
                                                Optional.of(editStopsList),
                                                new AccessibilityNodeInfoWrapper(editStopsList).getLastChild()),
                                        this::clickLastStopOrScrollToLastStop);
                            },
                            () -> {
                                if (cooldown.isCooldownOver()) {
                                    // Retry expansion click if the first one was ignored
                                    WaitingForStopCountClickHandler
                                            .findStopCountNode(root, googleMapsContext)
                                            .ifPresent(this::reclickStopCountNode);
                                }
                            });
        }

        private void clickLastStopOrScrollToLastStop(final AccessibilityNodeInfo editStopsList,
                                                     final AccessibilityNodeInfo lastStop) {
            if (listContainsCenterOfItem(editStopsList, lastStop)) {
                clickLastStop(lastStop);
            } else {
                scrollToLastStop(editStopsList);
            }
        }

        private void clickLastStop(final AccessibilityNodeInfo lastStop) {
            Log.d(TAG, "Step 2: Last waypoint is visible. Clicking...");
            if (new AccessibilityServiceWrapper(accessibilityService).click(lastStop)) {
                state = State.WAITING_FOR_CLEAR_CLICK;
                cooldown.startCooldown();
            }
        }

        private void scrollToLastStop(final AccessibilityNodeInfo editStopsList) {
            Log.d(TAG, "Step 2: Last waypoint is off-screen. Scrolling...");
            if (editStopsList.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                cooldown.startCooldown();
            }
        }

        private static Optional<AccessibilityNodeInfo> findEditStopsList(final AccessibilityNodeInfo root) {
            return new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId("com.google.android.apps.maps:id/edit_waypoints_list");
        }

        private void reclickStopCountNode(final AccessibilityNodeInfo stopCountNode) {
            Log.d(TAG, "Step 2: Re-clicking expansion label '" + googleMapsContext.stopsWord + "'...");
            if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountNode)) {
                cooldown.startCooldown();
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
    }

    private class WaitingForClearClickHandler {

        private final Cooldown cooldown;

        public WaitingForClearClickHandler(final Cooldown cooldown) {
            this.cooldown = cooldown;
        }

        public void handleWaitingForClearClick(final AccessibilityNodeInfo root) {
            final AccessibilityNodeInfoWrapper rootWrapper = new AccessibilityNodeInfoWrapper(root);
            WaitingForClearClickHandler
                    .findEditText(rootWrapper)
                    .ifPresentOrElse(
                            editText ->
                                    WaitingForClearClickHandler
                                            .findClearButton(rootWrapper)
                                            .ifPresentOrElse(
                                                    clearButton -> {
                                                        final String currentText = getText(editText);
                                                        if (textToClear.isEmpty()) {
                                                            textToClear = Optional.of(currentText);
                                                            Log.d(TAG, "Step 3: Dummy stop text identified: '" + currentText + "'");
                                                        }
                                                        if (!currentText.equals(textToClear.orElseThrow())) {
                                                            Log.d(TAG, "Step 3: Text has changed or was cleared. Stopping automation.");
                                                            finishAutomation();
                                                            return;
                                                        }
                                                        if (cooldown.isCooldownOver()) {
                                                            clickClearButton(clearButton);
                                                        }
                                                    },
                                                    () -> {
                                                        // Success: EditText is present but the clear button is missing, meaning the field is "empty"
                                                        Log.d(TAG, "Step 3: Clear button is gone. Automation completed successfully!");
                                                        finishAutomation();
                                                    }),
                            () -> Log.d(TAG, "Step 3: Search bar not found yet. Waiting for transition..."));
        }

        private void clickClearButton(final AccessibilityNodeInfo clearButton) {
            Log.d(TAG, "Step 3: Found clear button. Attempting to click...");
            if (new AccessibilityServiceWrapper(accessibilityService).click(clearButton)) {
                cooldown.startCooldown();
            }
        }

        private static String getText(final AccessibilityNodeInfo node) {
            return new AccessibilityNodeInfoWrapper(node)
                    .getText()
                    .map(CharSequence::toString)
                    .orElse("");
        }

        private static Optional<AccessibilityNodeInfo> findEditText(final AccessibilityNodeInfoWrapper rootWrapper) {
            return rootWrapper.findFirstAccessibilityNodeInfoByViewId("com.google.android.apps.maps:id/search_omnibox_edit_text");
        }

        private static Optional<AccessibilityNodeInfo> findClearButton(final AccessibilityNodeInfoWrapper rootWrapper) {
            return rootWrapper.findFirstAccessibilityNodeInfoByViewId("com.google.android.apps.maps:id/search_omnibox_text_clear");
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

    private void finishAutomation() {
        state = State.IDLE;
        watchdogHandler.removeCallbacksAndMessages(null);
        textToClear = Optional.empty();
    }

    public void reset() {
        if (state != State.IDLE) {
            Log.d(TAG, "Automation reset from state: " + state);
            finishAutomation();
        }
    }
}
