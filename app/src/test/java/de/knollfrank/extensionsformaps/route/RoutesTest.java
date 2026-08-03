package de.knollfrank.extensionsformaps.route;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class RoutesTest {

    @Test
    public void test_addDummyStop() {
        // Given
        final Route route = RouteTestFactory.createRouteWithTwoWaypoints();

        // When
        final Route routeWithDummyStop = Routes.addDummyStop(route);

        // Then
        assertEquals(route.stops().size() + 1, routeWithDummyStop.stops().size());
        assertMainListStartsWithPrefixList(routeWithDummyStop.stops(), route.stops());
    }

    private static void assertMainListStartsWithPrefixList(final List<Stop> mainList, final List<Stop> prefixList) {
        assertEquals(mainList.subList(0, prefixList.size()), prefixList);
    }
}