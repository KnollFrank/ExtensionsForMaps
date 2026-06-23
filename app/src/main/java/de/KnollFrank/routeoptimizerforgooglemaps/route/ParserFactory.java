package de.KnollFrank.routeoptimizerforgooglemaps.route;

class ParserFactory {

	public static Parser<Double> createLatitudeParser() {
		return createDoubleParser("3d");
	}

	public static Parser<Double> createLongitudeParser() {
		return createDoubleParser("4d");
	}

	private static Parser<Double> createDoubleParser(final String marker) {
		return new Parser<>() {

			@Override
			public boolean matches(final String token) {
				return token.startsWith(marker);
			}

			@Override
			public Double parse(final String token) {
				return Double.parseDouble(token.substring(marker.length()));
			}
		};
	}
}
