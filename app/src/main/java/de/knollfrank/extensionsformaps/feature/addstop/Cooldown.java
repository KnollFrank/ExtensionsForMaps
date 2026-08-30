package de.knollfrank.extensionsformaps.feature.addstop;

import java.time.Duration;
import java.time.Instant;

class Cooldown {

    private final Duration threshold;
    private Instant startCooldown = Instant.EPOCH;

    public Cooldown(final Duration threshold) {
        this.threshold = threshold;
    }

    public void startCooldown() {
        startCooldown = Instant.now();
    }

    public boolean isCooldownOver() {
        return Duration
                .between(startCooldown, Instant.now())
                .compareTo(threshold) >= 0;
    }

    public void resetCooldown() {
        startCooldown = Instant.EPOCH;
    }
}
