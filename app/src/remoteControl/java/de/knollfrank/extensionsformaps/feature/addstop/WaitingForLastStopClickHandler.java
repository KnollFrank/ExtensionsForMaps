package de.knollfrank.extensionsformaps.feature.addstop;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.util.Pair;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.AccessibilityServiceWrapper;
import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.common.Optionals;
import de.knollfrank.extensionsformaps.common.RectWrapper;

class WaitingForLastStopClickHandler {

    private final AccessibilityService accessibilityService;
    private final GoogleMapsContext googleMapsContext;
    private final Cooldown cooldown;
    private final StateHandler stateHandler;

    public WaitingForLastStopClickHandler(final AccessibilityService accessibilityService,
                                          final GoogleMapsContext googleMapsContext,
                                          final Cooldown cooldown,
                                          final StateHandler stateHandler) {
        this.accessibilityService = accessibilityService;
        this.googleMapsContext = googleMapsContext;
        this.cooldown = cooldown;
        this.stateHandler = stateHandler;
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

    private static Optional<AccessibilityNodeInfo> findEditStopsList(final AccessibilityNodeInfo root) {
        return new AccessibilityNodeInfoWrapper(root).findFirstAccessibilityNodeInfoByViewId(
                ResourceNameFactory.createGoogleMapsResourceName("edit_waypoints_list"));
    }

    private void clickLastStopOrScrollToLastStop(final AccessibilityNodeInfo editStopsList,
                                                 final AccessibilityNodeInfo lastStop) {
        if (listContainsCenterOfItem(editStopsList, lastStop)) {
            clickLastStop(lastStop);
        } else {
            scrollToLastStop(editStopsList);
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

    private void clickLastStop(final AccessibilityNodeInfo lastStop) {
        Log.d(AddStopAutomation.TAG, "Step 2: Last waypoint is visible. Clicking...");
        if (new AccessibilityServiceWrapper(accessibilityService).click(lastStop)) {
            stateHandler.state = State.WAITING_FOR_CLEAR_CLICK;
            cooldown.startCooldown();
        }
    }

    private void scrollToLastStop(final AccessibilityNodeInfo editStopsList) {
        Log.d(AddStopAutomation.TAG, "Step 2: Last waypoint is off-screen. Scrolling...");
        if (editStopsList.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
            cooldown.startCooldown();
        }
    }

    private void reclickStopCountNode(final AccessibilityNodeInfo stopCountNode) {
        Log.d(AddStopAutomation.TAG, "Step 2: Re-clicking expansion label '" + googleMapsContext.stopsWord + "'...");
        if (new AccessibilityServiceWrapper(accessibilityService).click(stopCountNode)) {
            cooldown.startCooldown();
        }
    }
}
