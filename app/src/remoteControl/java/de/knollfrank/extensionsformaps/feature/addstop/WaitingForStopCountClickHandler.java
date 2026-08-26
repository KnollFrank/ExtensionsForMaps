package de.knollfrank.extensionsformaps.feature.addstop;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;

class WaitingForStopCountClickHandler {

    private final AccessibilityService accessibilityService;
    private final GoogleMapsContext googleMapsContext;
    private final Cooldown cooldown;
    private final StateHandler stateHandler;

    public WaitingForStopCountClickHandler(final AccessibilityService accessibilityService,
                                           final GoogleMapsContext googleMapsContext,
                                           final Cooldown cooldown,
                                           final StateHandler stateHandler) {
        this.accessibilityService = accessibilityService;
        this.googleMapsContext = googleMapsContext;
        this.cooldown = cooldown;
        this.stateHandler = stateHandler;
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
        Log.d(AddStopAutomation.TAG, "Step 1 (Backup): Found '" + googleMapsContext.stopsWord + "' label. Clicking...");
        if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountNode)) {
            stateHandler.state = StateHandler.State.WAITING_FOR_LAST_STOP_CLICK;
            cooldown.startCooldown();
        }
    }
}
