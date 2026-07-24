package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.AbstractJob;
import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroup;

class FlexibleGroupConstraint implements SoftActivityConstraint {

    private static final double GROUP_VIOLATION_PENALTY = 1_000_000.0;
    private static final int NO_GROUP_SEQUENCE_ORDER = 9999;

    // FK-TODO: refactor
    @Override
    public double getCosts(final JobInsertionContext context,
                           final TourActivity prevAct,
                           final TourActivity newAct,
                           final TourActivity nextAct,
                           final double prevActDepTime) {

        final int newJobOrder = getSequenceOrderOrDefault(context.getJob());
        double totalPenalty = 0.0;
        final VehicleRoute route = context.getRoute();
        boolean pastInsertionPoint = false;
        for (final TourActivity act : route.getActivities()) {
            if (act == nextAct) {
                pastInsertionPoint = true;
            }
            if (act instanceof final TourActivity.JobActivity jobAct) {
                final int existingJobOrder = getSequenceOrderOrDefault(jobAct.getJob());
                if (!pastInsertionPoint) {
                    // Vor dem neuen Job darf kein Job mit HÖHEREM Index liegen (z.B. Dorf vor Stadt)
                    if (existingJobOrder > newJobOrder) {
                        totalPenalty += GROUP_VIOLATION_PENALTY;
                    }
                } else {
                    // Nach dem neuen Job darf kein Job mit NIEDRIGEREM Index liegen (z.B. Stadt nach Dorf)
                    if (existingJobOrder < newJobOrder) {
                        totalPenalty += GROUP_VIOLATION_PENALTY;
                    }
                }
            }
        }
        return totalPenalty;
    }

    private static int getSequenceOrderOrDefault(final Job job) {
        if (job instanceof final AbstractJob abstractJob) {
            final Object userData = abstractJob.getUserData();
            if (userData instanceof Optional<?> opt) {
                return opt
                        .filter(DeliveryGroup.class::isInstance)
                        .map(DeliveryGroup.class::cast)
                        .map(DeliveryGroup::sequenceOrder)
                        .orElse(NO_GROUP_SEQUENCE_ORDER);
            } else if (userData instanceof DeliveryGroup group) {
                return group.sequenceOrder();
            }
        }
        return NO_GROUP_SEQUENCE_ORDER;
    }
}
