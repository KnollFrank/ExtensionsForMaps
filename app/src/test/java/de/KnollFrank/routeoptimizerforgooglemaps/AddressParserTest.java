package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AddressParserTest {

	@Test
	public void testParseAddress_withUrl() {
		final String shareText = "Brandenburger Tor \n Pariser Platz, 10117 Berlin \n https://maps.app.goo.gl/xyz123";
		final String result = MainActivity.parseAddress(shareText);
		assertEquals("Brandenburger Tor, Pariser Platz, 10117 Berlin", result);
	}

	@Test
	public void testParseAddress_onlyUrl() {
		final String shareText = "https://maps.app.goo.gl/xyz123";
		final String result = MainActivity.parseAddress(shareText);
		assertEquals("", result);
	}

	@Test
	public void testParseAddress_noUrl() {
		final String shareText = "Alexanderplatz, 10178 Berlin";
		final String result = MainActivity.parseAddress(shareText);
		assertEquals("Alexanderplatz, 10178 Berlin", result);
	}
}
