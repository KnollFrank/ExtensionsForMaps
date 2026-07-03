package de.KnollFrank.routeoptimizerforgooglemaps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;

@RunWith(RobolectricTestRunner.class)
public class DirectionsUrlTemplateFactoryTest {

    @Test
    public void test_createDirectionsUrlTemplate() throws MalformedURLException {
        final URL directionsUrlTemplate =
                DirectionsUrlTemplateFactory.createDirectionsUrlTemplate(
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.50248706742132, Unit.DEGREES),
                                new Angle(8.992563508173783, Unit.DEGREES)),
                        27);
        System.out.println(directionsUrlTemplate);
    }
}