package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Base64;

import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.Datatype;
import de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf.Node;

class PlaceIdParser {

    public static boolean isPlaceIdNode(final Node node) {
        return node.fieldId == 1 && Datatype.STRING.equals(node.datatype);
    }

    public static String getPlaceId(final Node node) {
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

    /**
     * Konvertiert eine Standard-Web-API Place-ID ("ChIJ...") zurück in das
     * interne Google Maps Hex-Format ("0x...:0x..."), das im data-Part benötigt wird.
     */
    // FK-TODO: refactor
    public static String convertPlaceIdToHex(final String placeId) {
        if (placeId == null) {
            return null;
        }
        // Google Place-IDs im Web-Format beginnen aufgrund der festen Protobuf-Header (0x0A, 0x12, 0x09) immer mit "ChI"
        if (!placeId.startsWith("ChI")) {
            return placeId;
        }

        try {
            // 1. Base64-URL-Decoder ohne Padding anwenden
            final byte[] proto = Base64.getUrlDecoder().decode(placeId);
            if (proto.length < 20) {
                return placeId;
            }

            // 2. Rekonstruktion der Cell ID (Bytes 3 bis 10) - Little Endian wieder zusammensetzen
            long cellId = 0;
            for (int i = 0; i < 8; i++) {
                cellId |= ((long) (proto[3 + i] & 0xFF)) << (8 * i);
            }

            // 3. Rekonstruktion der Feature ID (Bytes 12 bis 19) - Little Endian wieder zusammensetzen
            long featureId = 0;
            for (int i = 0; i < 8; i++) {
                featureId |= ((long) (proto[12 + i] & 0xFF)) << (8 * i);
            }

            // 4. Als vorzeichenlose Hex-Strings im geforderten Zielformat ausgeben
            return "0x" + Long.toUnsignedString(cellId, 16) + ":0x" + Long.toUnsignedString(featureId, 16);
        } catch (final Exception e) {
            // Fallback auf die originale ID, falls beim Parsen etwas schiefgeht
            return placeId;
        }
    }
}
