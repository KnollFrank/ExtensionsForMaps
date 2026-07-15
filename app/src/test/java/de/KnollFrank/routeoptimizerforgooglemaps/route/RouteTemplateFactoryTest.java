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
        // Given
        final int numberOfStops = 27;

        // When
        final Route routeTemplate = RouteTemplateFactory.createRouteTemplate(numberOfStops);

        // Then
        assertEquals(numberOfStops, routeTemplate.stops().size());
    }

    @Test
    public void test_createRouteTemplate_numberOfStops_11() {
        // Given
        final int numberOfStops = 11;

        // When
        final Route routeTemplate = RouteTemplateFactory.createRouteTemplate(numberOfStops);

        // Then
        assertEquals(numberOfStops, routeTemplate.stops().size());
    }

    @Test
    public void test_createRouteTemplate_numberOfStops_lessThan2_forbidden() {
        // Given
        final int numberOfStops = 1;

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> RouteTemplateFactory.createRouteTemplate(numberOfStops));
    }

    @Test
    public void test_createRouteTemplate_numberOfStops_greaterThan27_forbidden() {
        // Given
        final int numberOfStops = 28;

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> RouteTemplateFactory.createRouteTemplate(numberOfStops));
    }
}
