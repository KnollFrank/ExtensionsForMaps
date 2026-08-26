package de.knollfrank.extensionsformaps.feature.addstop;

class Cooldown {

    // FK-TODO: use java.time
    private final long thresholdMillis;
    private long startCooldownTimeMillis = 0;

    public Cooldown(final long thresholdMillis) {
        this.thresholdMillis = thresholdMillis;
    }

    public void startCooldown() {
        startCooldownTimeMillis = System.currentTimeMillis();
    }

    public boolean isCooldownOver() {
        return System.currentTimeMillis() - startCooldownTimeMillis >= thresholdMillis;
    }

    public void resetCooldown() {
        startCooldownTimeMillis = 0;
    }
}
