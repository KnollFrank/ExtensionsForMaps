package de.KnollFrank.routeoptimizerforgooglemaps;

record TimeWindow<T>(T start, T end) {

    public TimeWindow<T> withStart(final T newStart) {
        return new TimeWindow<>(newStart, end());
    }

    public TimeWindow<T> withEnd(final T newEnd) {
        return new TimeWindow<>(start(), newEnd);
    }
}
