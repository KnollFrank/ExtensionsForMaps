package de.KnollFrank.routeoptimizerforgooglemaps.route;

import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.Datatype;
import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.Node;

class PlaceIdParser {

    public static boolean isPlaceIdNode(final Node node) {
        return node.fieldId == 1 && Datatype.STRING.equals(node.datatype);
    }

    public static OfficialPlaceId getOfficialPlaceId(final Node node) {
        return new UndocumentedPlaceId(node.value).toOfficialPlaceId();
    }
}
