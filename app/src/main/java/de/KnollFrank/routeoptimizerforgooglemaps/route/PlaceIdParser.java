package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Base64;

class PlaceIdParser implements Parser<String> {

	private static final String placeIdMarker = MarkerFactory.createMarker(1, Datatype.STRING);

	@Override
	public boolean matches(final String token) {
		return token.startsWith(placeIdMarker);
	}

	@Override
	public String parse(final String token) {
		return convertHexToPlaceId(token.substring(placeIdMarker.length()));
	}

	/**
	 * Converts an internal Google Maps Hex-ID into a standard Web-API Place ID ("ChIJ...").
	 */
	// FK-TODO: add unit test
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
