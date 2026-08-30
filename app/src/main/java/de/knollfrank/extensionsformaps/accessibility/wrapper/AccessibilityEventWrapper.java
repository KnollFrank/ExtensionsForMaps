package de.knollfrank.extensionsformaps.accessibility.wrapper;

import android.view.accessibility.AccessibilityEvent;

import java.util.Optional;

public record AccessibilityEventWrapper(AccessibilityEvent event) {

    public Optional<String> getPackageName() {
        return Optional
                .ofNullable(event.getPackageName())
                .map(CharSequence::toString);
    }
}
