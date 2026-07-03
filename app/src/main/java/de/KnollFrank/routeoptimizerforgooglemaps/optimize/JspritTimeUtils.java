package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class JspritTimeUtils {

    private JspritTimeUtils() {
    }

    /**
     * Konvertiert ein LocalDateTime-Paar in ein jsprit TimeWindow.
     * Fängt MIN und MAX ab, um unbegrenzte Zeitfenster sicher darzustellen.
     */
    // FK-TODO: refactor, introduce own TimeWindow e.g.org.threeten.extra.Interval
    public static TimeWindow toJspritWindow(final LocalDateTime start, final LocalDateTime end) {
        return TimeWindow.newInstance(
                start.equals(LocalDateTime.MIN)
                        ? 0.0  // jsprit-sicherer Nullpunkt (Kunde/Fahrzeug ist "ab sofort" bereit)
                        : start.toEpochSecond(ZoneOffset.UTC),
                end.equals(LocalDateTime.MAX)
                        ? Double.MAX_VALUE // Echtes unendliches Ende für den Solver
                        : end.toEpochSecond(ZoneOffset.UTC));
    }
}