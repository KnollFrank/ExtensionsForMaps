package de.KnollFrank.routeoptimizerforgooglemaps.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class RouteTemplateFactoryTest {

    @Test
    public void test_createRouteTemplate_numberOfStops_27() {
        test_createRouteTemplate(27);
    }

    @Test
    public void test_createRouteTemplate_numberOfStops_11() {
        test_createRouteTemplate(11);
    }

    @Test
    public void test_createRouteTemplate_numberOfStops_lessThan2_forbidden() {
        assertIsForbidden(1);
    }

    @Test
    public void test_createRouteTemplate_numberOfStops_greaterThan27_forbidden() {
        assertIsForbidden(28);
    }

    private static void test_createRouteTemplate(final int numberOfStops) {
        // When
        final Route routeTemplate = RouteTemplateFactory.createRouteTemplate(numberOfStops);

        // Then
        assertEquals(numberOfStops, routeTemplate.stops().size());
    }

    private static void assertIsForbidden(final int numberOfStops) {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> RouteTemplateFactory.createRouteTemplate(numberOfStops));
    }
}
