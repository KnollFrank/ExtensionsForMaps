package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Base64;

import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.Node;

class PlaceIdParser {

    public boolean isPlaceIdNode(final Node node) {
        return node.fieldId == 1 && node.dataType == Datatype.STRING.marker();
    }

    public String getPlaceId(final Node node) {
        return convertHexToPlaceId(node.value);
    }

    /**
     * Converts an internal Google Maps Hex-ID into a standard Web-API Place ID ("ChIJ...").
     */
    // FK-TODO: add unit test
    // FK-TODO: refactor
    private static String convertHexToPlaceId(final String internalId) {
        if (internalId == null || !internalId.contains(":")) {
            return internalId;
        }

        try {
            final String[] parts = internalId.split(":");
            long cellId = Long.parseUnsignedLong(parts[0].replace("0x", ""), 16);
            long featureId = Long.parseUnsignedLong(parts[1].replace("0x", ""), 16);

            byte[] proto = new byte[20];
            proto[0] = 0x0A;
            proto[1] = 0x12;
            proto[2] = 0x09;

            for (int i = 0; i < 8; i++) {
                proto[3 + i] = (byte) (cellId >> (8 * i));
            }

            proto[11] = 0x11;

            for (int i = 0; i < 8; i++) {
                proto[12 + i] = (byte) (featureId >> (8 * i));
            }

            return Base64.getUrlEncoder().withoutPadding().encodeToString(proto);
        } catch (final Exception e) {
            return internalId;
        }
    }
}
