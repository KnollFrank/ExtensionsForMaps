package de.KnollFrank.routeoptimizerforgooglemaps.route;

public class DeliveryGroups {

    public static final DeliveryGroup TOWN =
            new DeliveryGroup(
                    "TOWN",
                    "Stadt (zuerst beliefern)",
                    1);
    public static final DeliveryGroup VILLAGE =
            new DeliveryGroup(
                    "VILLAGE",
                    "Dorf (als zweites beliefern)",
                    2);
}
