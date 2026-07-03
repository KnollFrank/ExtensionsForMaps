package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.AbstractJob;
import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroup;

public class FlexibleGroupConstraint implements SoftActivityConstraint {

    // Anforderung 4 & 5: Strafe dominiert Kilometerkosten, 
    // ist aber WEIT unter der Strafe für den Ausfall eines Jobs (1.000.000)
    private static final double GROUP_VIOLATION_PENALTY = 50_000.0;

    @Override
    public double getCosts(final JobInsertionContext context,
                           final TourActivity prevAct,
                           final TourActivity newAct,
                           final TourActivity nextAct,
                           final double prevActDepTime) {

        // Anforderung 3: Wir holen uns die Gruppen-ID, die wir im Priority-Feld geparkt haben
        final Optional<Integer> newJobGroupOrder = getSequenceOrder(context.getJob());
        double totalPenalty = 0.0;
        final VehicleRoute route = context.getRoute();

        boolean pastInsertionPoint = false;

        // Wir scannen die Route, um Gruppenkollisionen zu identifizieren
        for (final TourActivity act : route.getActivities()) {
            if (act == nextAct) {
                pastInsertionPoint = true;
            }
            if (act instanceof final TourActivity.JobActivity jobAct) {
                final Optional<Integer> existingJobGroupOrder = getSequenceOrder(jobAct.getJob());
                if (!pastInsertionPoint) {
                    // VOR dem Einfügepunkt darf keine logisch spätere Gruppe liegen
                    // (z.B. Dorf [2] darf nicht vor Kernstadt [1] beliefert werden)
                    if (existingJobGroupOrder.isPresent() && newJobGroupOrder.isPresent() && existingJobGroupOrder.orElseThrow() > newJobGroupOrder.orElseThrow()) {
                        totalPenalty += GROUP_VIOLATION_PENALTY;
                    }
                } else {
                    // NACH dem Einfügepunkt darf keine logisch frühere Gruppe liegen
                    // (z.B. Kernstadt [1] darf nicht hinter einem Dorf [2] liegen)
                    if (existingJobGroupOrder.isPresent() && newJobGroupOrder.isPresent() && existingJobGroupOrder.orElseThrow() < newJobGroupOrder.orElseThrow()) {
                        totalPenalty += GROUP_VIOLATION_PENALTY;
                    }
                }
            }
        }
        return totalPenalty;
    }

    private static Optional<Integer> getSequenceOrder(final Job job) {
        if (job instanceof final AbstractJob abstractJob) {
            if (abstractJob.getUserData() instanceof Optional) {
                final Optional<DeliveryGroup> group = (Optional<DeliveryGroup>) abstractJob.getUserData();
                return group.map(DeliveryGroup::sequenceOrder);
            }
        }
        throw new IllegalStateException();
    }
}
