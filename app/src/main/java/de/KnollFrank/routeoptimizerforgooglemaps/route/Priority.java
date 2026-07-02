package de.KnollFrank.routeoptimizerforgooglemaps.route;

import androidx.annotation.NonNull;

public enum Priority {

    _1_VeryHigh(1, "1 - very high"),
    _2_Default(2, "2 - default"),
    _3(3, "3"),
    _4(4, "4"),
    _5(5, "5"),
    _6(6, "6"),
    _7(7, "7"),
    _8(8, "8"),
    _9(9, "9"),
    _10_VeryLow(10, "10 - very low");

    public final int priority;
    public final String name;

    Priority(final int priority, final String name) {
        this.priority = priority;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
