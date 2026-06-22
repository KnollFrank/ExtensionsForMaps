package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class SimpleGmapsRouteParserTest {

	@Test
	public void testParseRoute_AllCoordinatesInData_ShouldPass() {
		String url = "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0";

		List<SimpleGmapsRouteParser.Waypoint> result = SimpleGmapsRouteParser.parseRoute(url);

		assertEquals(3, result.size());

		// Check Stop 1 (Central-Apotheke)
		assertEquals(48.4765345, result.get(0).lat, 0.000001);
		assertEquals(8.934900899999999, result.get(0).lng, 0.000001);
		assertEquals("ChIJ1V1RE0v8mUcROpsR_6oBUjQ", result.get(0).placeId);

		// Check Stop 2 (Hamburg)
		assertEquals(53.548828199999996, result.get(1).lat, 0.000001);
		assertEquals(9.987170299999999, result.get(1).lng, 0.000001);
		assertEquals("ChIJuRMYfoNhsUcRoDrWe_I9JgQ", result.get(1).placeId);
	}

	@Test
	public void testParseRoute_AllCoordinatesInPath_ShouldPass() {
		String url = "https://www.google.com/maps/dir/48.5015274,8.9932287/48.4765345,8.9349009/48.4752669,8.9284933/@48.4884161,8.9604793,13z/data=!4m4!4m3!2m1!2b1!3e0/dir/48.5015274,8.9932287/48.4765345,8.9349009/48.4752669,8.9284933/@48.4884161,8.9604793,13z/data=!4m4!4m3!2m1!2b1!3e0";

		List<SimpleGmapsRouteParser.Waypoint> result = SimpleGmapsRouteParser.parseRoute(url);

		assertEquals(3, result.size());

		assertEquals(48.5015274, result.get(0).lat, 0.000001);
		assertEquals(8.9932287, result.get(0).lng, 0.000001);
		assertNull(result.get(0).placeId);

		assertEquals(48.4765345, result.get(1).lat, 0.000001);
		assertEquals(8.9349009, result.get(1).lng, 0.000001);
	}

	@Test
	public void testParseRoute_MissingCoordinates_ShouldThrowException() {
		String url = "https://www.google.com/maps/dir/48.4820178,8.9373542/Central-Apotheke,+Marktstra%C3%9Fe+17,+72108+Rottenburg+am+Neckar/Am+Berg+9,+72181+Starzach/@48.454927,8.8748639,11z/data=!4m11!4m10!1m0!1m2!1m1!1s0x4799fc4b13515dd5:0x345201aaff119b3a!1m2!1m1!1s0x4797544e94af23df:0x4bcdf7205ebe2426!2m1!2b1!3e0/dir/48.4820178,8.9373542/Central-Apotheke,+Marktstra%C3%9Fe+17,+72108+Rottenburg+am+Neckar/Am+Berg+9,+72181+Starzach/@48.454927,8.8748639,11z/data=!4m11!4m10!1m0!1m2!1m1!1s0x4799fc4b13515dd5:0x345201aaff119b3a!1m2!1m1!1s0x4797544e94af23df:0x4bcdf7205ebe2426!2m1!2b1!3e0";

		SimpleGmapsRouteParser.MissingCoordinateException exception = assertThrows(
				SimpleGmapsRouteParser.MissingCoordinateException.class,
				() -> SimpleGmapsRouteParser.parseRoute(url)
		);

		assertTrue(exception.getMessage().contains("Stop 2"));
		assertTrue(exception.getMessage().contains("Central-Apotheke"));
	}

	@Test
	public void testParseRoute_EmptyUrl_ShouldReturnEmptyList() {
		String url = "";
		List<SimpleGmapsRouteParser.Waypoint> result = SimpleGmapsRouteParser.parseRoute(url);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testParseRoute_UrlWithTrailingQueryParams_ShouldPass() {
		String url = "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0";

		List<SimpleGmapsRouteParser.Waypoint> result = SimpleGmapsRouteParser.parseRoute(url);

		assertEquals(3, result.size());

		// Check Stop 1
		assertEquals("Central-Apotheke", result.get(0).pathName);
		assertEquals(48.4765345, result.get(0).lat, 0.000001);
		assertEquals(8.934900899999999, result.get(0).lng, 0.000001);
		assertEquals("ChIJ1V1RE0v8mUcROpsR_6oBUjQ", result.get(0).placeId);

		// Check Stop 3 (Unterhausen)
		assertEquals("Unterhausen", result.get(2).pathName);
		assertEquals(48.430628399999996, result.get(2).lat, 0.000001);
		assertEquals(9.2546378, result.get(2).lng, 0.000001);
		assertEquals("ChIJsYBbyF7zmUcREc3DW6XSMuQ", result.get(2).placeId);
	}

	@Test
	public void testParseRoute_InvalidUrlFormat_ShouldThrowIllegalArgumentException() {
		String invalidUrl = "https://www.google.com/maps/place/Berlin/data=!3m1!4b1!4m6!3m5!1s0x47a84e373f035901:0x42120465b5e3b70/place/Central-Apotheke/";

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> SimpleGmapsRouteParser.parseRoute(invalidUrl)
		);

		assertTrue(exception.getMessage().contains("Invalid URL"));
	}
}