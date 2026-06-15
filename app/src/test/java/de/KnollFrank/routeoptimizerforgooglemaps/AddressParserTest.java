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

	@Test
	public void testParseAddress_longGoogleMapsUrl() {
		final String longUrl = "https://www.google.com/maps/place/Goetheanum,+R%C3%BCttiweg+45,+4143+Dornach,+Switzerland/data=!4m2!3m1!1s0x4791c871e14eee41:0x28bc250fed7c57c";
		final String result = MainActivity.parseAddress(longUrl);
		assertEquals("Goetheanum, Rüttiweg 45, 4143 Dornach, Switzerland", result);
	}
}
