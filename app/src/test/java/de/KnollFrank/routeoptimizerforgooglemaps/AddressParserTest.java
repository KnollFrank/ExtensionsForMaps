package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class AddressParserTest {

	@Test
	public void testParseAddresses_withUrl() {
		final String shareText = "Brandenburger Tor \n Pariser Platz, 10117 Berlin \n https://maps.app.goo.gl/xyz123";
		final List<String> result = MainActivity.parseAddresses(shareText);
		assertEquals(1, result.size());
		assertEquals("Brandenburger Tor, Pariser Platz, 10117 Berlin", result.get(0));
	}

	@Test
	public void testParseAddresses_multiStopUrl() {
		final String multiUrl = "https://www.google.com/maps/dir/Berlin/Leipzig/Munich/data=!4m2!3m1!1s0x...";
		final List<String> result = MainActivity.parseAddresses(multiUrl);
		assertEquals(List.of("Berlin", "Leipzig", "Munich"), result);
	}

	@Test
	public void testParseAddresses_shareDirectionsText() {
		final String text = "Shared route\nFrom Berlin to Munich\n\n1. Head south\n4. Arrive at location: Leipzig\n12. Arrive at location: Munich";
		final List<String> result = MainActivity.parseAddresses(text);
		assertEquals(List.of("Leipzig", "Munich"), result);
	}

	@Test
	public void testParseAddresses_longGoogleMapsUrl() {
		final String longUrl = "https://www.google.com/maps/place/Goetheanum,+R%C3%BCttiweg+45,+4143+Dornach,+Switzerland/data=!4m2!3m1!1s0x4791c871e14eee41:0x28bc250fed7c57c";
		final List<String> result = MainActivity.parseAddresses(longUrl);
		assertEquals(List.of("Goetheanum, Rüttiweg 45, 4143 Dornach, Switzerland"), result);
	}
}
