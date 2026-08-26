package de.knollfrank.extensionsformaps.feature.addstop;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.AccessibilityNodeInfoWrapper;
import de.knollfrank.extensionsformaps.accessibility.AccessibilityServiceWrapper;

class WaitingForClearClickHandler {

    private final AccessibilityService accessibilityService;
    private final Cooldown cooldown;
    private final Runnable finishAutomation;
    private Optional<String> textToClear = Optional.empty();

    public WaitingForClearClickHandler(final AccessibilityService accessibilityService,
                                       final Cooldown cooldown,
                                       final Runnable finishAutomation) {
        this.accessibilityService = accessibilityService;
        this.cooldown = cooldown;
        this.finishAutomation = finishAutomation;
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
                                                        Log.d(AddStopAutomation.TAG, "Step 3: Dummy stop text identified: '" + currentText + "'");
                                                    }
                                                    if (!currentText.equals(textToClear.orElseThrow())) {
                                                        Log.d(AddStopAutomation.TAG, "Step 3: Text has changed or was cleared. Stopping automation.");
                                                        finishAutomation.run();
                                                        return;
                                                    }
                                                    if (cooldown.isCooldownOver()) {
                                                        clickClearButton(clearButton);
                                                    }
                                                },
                                                () -> {
                                                    // Success: EditText is present but the clear button is missing, meaning the field is "empty"
                                                    Log.d(AddStopAutomation.TAG, "Step 3: Clear button is gone. Automation completed successfully!");
                                                    finishAutomation.run();
                                                }),
                        () -> Log.d(AddStopAutomation.TAG, "Step 3: Search bar not found yet. Waiting for transition..."));
    }

    public void resetTextToClear() {
        textToClear = Optional.empty();
    }

    private void clickClearButton(final AccessibilityNodeInfo clearButton) {
        Log.d(AddStopAutomation.TAG, "Step 3: Found clear button. Attempting to click...");
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
