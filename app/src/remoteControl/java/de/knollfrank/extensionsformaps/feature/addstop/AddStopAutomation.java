package de.knollfrank.extensionsformaps.feature.addstop;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.time.Duration;

import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityServiceWrapper;

class AddStopAutomation {

    public static final String TAG = AddStopAutomation.class.getSimpleName();
    private static final Duration WATCHDOG_DELAY = Duration.ofMillis(1200);

    private final AccessibilityService accessibilityService;
    private final Handler watchdogHandler = new Handler(Looper.getMainLooper());
    private final StateHandler stateHandler = new StateHandler();
    private final Cooldown cooldown = new Cooldown(Duration.ofSeconds(1));
    private final WaitingForStopCountClickHandler waitingForStopCountClickHandler;
    private final WaitingForLastStopClickHandler waitingForLastStopClickHandler;
    private final WaitingForClearClickHandler waitingForClearClickHandler;

    public AddStopAutomation(final AccessibilityService accessibilityService,
                             final GoogleMapsContext googleMapsContext) {
        this.accessibilityService = accessibilityService;
        this.waitingForStopCountClickHandler =
                new WaitingForStopCountClickHandler(
                        accessibilityService,
                        googleMapsContext,
                        cooldown,
                        stateHandler);
        this.waitingForLastStopClickHandler =
                new WaitingForLastStopClickHandler(
                        accessibilityService,
                        googleMapsContext,
                        cooldown,
                        stateHandler);
        this.waitingForClearClickHandler =
                new WaitingForClearClickHandler(
                        accessibilityService,
                        cooldown,
                        this::finishAutomation);
    }

    public void start() {
        Log.d(TAG, "Automation started: WAITING_FOR_STOP_COUNT_CLICK");
        stateHandler.state = State.WAITING_FOR_STOP_COUNT_CLICK;
        cooldown.resetCooldown();
        waitingForClearClickHandler.resetTextToClear();
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
                    waitingForStopCountClickHandler.handleWaitingForStopCountClick(root);
            case WAITING_FOR_LAST_STOP_CLICK ->
                    waitingForLastStopClickHandler.handleWaitingForLastStopClick(root);
            case WAITING_FOR_CLEAR_CLICK ->
                    waitingForClearClickHandler.handleWaitingForClearClick(root);
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
                WATCHDOG_DELAY.toMillis());
    }

    private void finishAutomation() {
        stateHandler.state = State.IDLE;
        watchdogHandler.removeCallbacksAndMessages(null);
        waitingForClearClickHandler.resetTextToClear();
    }

    public void reset() {
        if (stateHandler.state != State.IDLE) {
            Log.d(TAG, "Automation reset from state: " + stateHandler.state);
            finishAutomation();
        }
    }
}
