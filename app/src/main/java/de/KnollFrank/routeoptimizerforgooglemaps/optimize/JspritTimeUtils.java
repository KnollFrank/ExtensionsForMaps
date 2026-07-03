package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.google.common.collect.Range;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class JspritTimeUtils {

    private JspritTimeUtils() {
    }

    public static TimeWindow toJspritWindow(final Range<LocalDateTime> range) {
        return TimeWindow.newInstance(getStart(range), getEnd(range));
    }

    private static double getStart(final Range<LocalDateTime> range) {
        return range.hasLowerBound()
                ? getEpochSecond(range.lowerEndpoint())
                : 0.0;
    }

    private static double getEnd(final Range<LocalDateTime> range) {
        return range.hasUpperBound()
                ? getEpochSecond(range.upperEndpoint())
                : Double.MAX_VALUE;
    }

    private static long getEpochSecond(final LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZoneOffset.UTC);
    }
}