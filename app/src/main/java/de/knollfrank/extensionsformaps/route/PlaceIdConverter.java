package de.knollfrank.extensionsformaps.route;

import java.util.Base64;

class PlaceIdConverter {

    // FK-TODO: add unit test
    // FK-TODO: refactor
    public static OfficialPlaceId toOfficialPlaceId(final UndocumentedPlaceId undocumentedPlaceId) {
        final String[] parts = undocumentedPlaceId.value().split(":");
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

        return new OfficialPlaceId(Base64.getUrlEncoder().withoutPadding().encodeToString(proto));
    }

    // FK-TODO: refactor
    public static UndocumentedPlaceId toUndocumentedPlaceId(final OfficialPlaceId officialPlaceId) {
        // 1. Base64-URL-Decoder ohne Padding anwenden
        final byte[] proto = Base64.getUrlDecoder().decode(officialPlaceId.value());
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
        return new UndocumentedPlaceId("0x" + Long.toUnsignedString(cellId, 16) + ":0x" + Long.toUnsignedString(featureId, 16));
    }
}
