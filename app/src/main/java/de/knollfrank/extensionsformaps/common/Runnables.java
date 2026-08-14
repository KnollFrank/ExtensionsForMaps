package de.knollfrank.extensionsformaps.common;

public class Runnables {

    private Runnables() {
    }

    public static Runnable empty() {
        return () -> {
        };
    }
}
