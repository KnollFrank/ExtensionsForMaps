package de.knollfrank.extensionsformaps.feature.addstop;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;

class AddStopAutomation {

    public static final String TAG = AddStopAutomation.class.getSimpleName();
    private static final long COOLDOWN_MS = 1000;
    private static final long WATCHDOG_DELAY_MS = 1200;

    private final AccessibilityService accessibilityService;
    private final GoogleMapsContext googleMapsContext;
    private final Handler watchdogHandler = new Handler(Looper.getMainLooper());
    private final StateHandler stateHandler = new StateHandler();
    private Optional<String> textToClear = Optional.empty();
    private final Cooldown cooldown = new Cooldown(COOLDOWN_MS);

    public AddStopAutomation(final AccessibilityService accessibilityService, final GoogleMapsContext googleMapsContext) {
        this.accessibilityService = accessibilityService;
        this.googleMapsContext = googleMapsContext;
    }

    public void start() {
        Log.d(TAG, "Automation started: WAITING_FOR_STOP_COUNT_CLICK");
        stateHandler.state = State.WAITING_FOR_STOP_COUNT_CLICK;
        cooldown.resetCooldown();
        textToClear = Optional.empty();
        scheduleWatchdog();
    }

    public void onStopCountUpdated(final Rect stopCountBounds) {
        if (stateHandler.state == State.WAITING_FOR_STOP_COUNT_CLICK && cooldown.isCooldownOver()) {
            Log.d(TAG, "Step 1: Clicking stop count label via onStopCountUpdated at " + stopCountBounds);
            if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountBounds)) {
                stateHandler.state = State.WAITING_FOR_LAST_STOP_CLICK;
                cooldown.startCooldown();
            }
        }
    }

    public void onGoogleMapsEvent(final AccessibilityNodeInfo root) {
        if (stateHandler.state == State.IDLE) {
            return;
        }
        processState(root);
    }

    private void processState(final AccessibilityNodeInfo root) {
        switch (stateHandler.state) {
            case WAITING_FOR_STOP_COUNT_CLICK ->
                    new WaitingForStopCountClickHandler(accessibilityService, googleMapsContext, cooldown, stateHandler).handleWaitingForStopCountClick(root);
            case WAITING_FOR_LAST_STOP_CLICK ->
                    new WaitingForLastStopClickHandler(accessibilityService, googleMapsContext, cooldown, stateHandler).handleWaitingForLastStopClick(root);
            case WAITING_FOR_CLEAR_CLICK ->
                    new WaitingForClearClickHandler(cooldown).handleWaitingForClearClick(root);
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
                    if (stateHandler.state != State.IDLE) {
                        Log.v(TAG, "Watchdog triggered check for state: " + stateHandler.state);
                        new AccessibilityServiceWrapper(accessibilityService)
                                .getRootInActiveWindow()
                                .ifPresent(this::processState);
                        scheduleWatchdog();
                    }
                },
                WATCHDOG_DELAY_MS);
    }

    private void finishAutomation() {
        stateHandler.state = State.IDLE;
        watchdogHandler.removeCallbacksAndMessages(null);
        textToClear = Optional.empty();
    }

    public void reset() {
        if (stateHandler.state != State.IDLE) {
            Log.d(TAG, "Automation reset from state: " + stateHandler.state);
            finishAutomation();
        }
    }
}
