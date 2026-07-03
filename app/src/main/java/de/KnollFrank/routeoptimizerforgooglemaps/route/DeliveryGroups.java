package de.KnollFrank.routeoptimizerforgooglemaps.route;

public class DeliveryGroups {

    public static final DeliveryGroup KERNSTADT =
            new DeliveryGroup(
                    "ks",
                    "Kernstadt (zuerst beliefern)",
                    1);
    public static final DeliveryGroup DOERFER =
            new DeliveryGroup(
                    "df",
                    "Dörfer (als zweites beliefern)",
                    2);
}
