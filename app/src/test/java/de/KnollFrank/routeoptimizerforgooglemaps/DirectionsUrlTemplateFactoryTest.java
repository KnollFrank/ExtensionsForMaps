package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;

@RunWith(RobolectricTestRunner.class)
public class DirectionsUrlTemplateFactoryTest {

    @Test
    public void test_createDirectionsUrlTemplate() {
        // Given
        final DirectionsUrlTemplateFactory directionsUrlTemplateFactory =
                new DirectionsUrlTemplateFactory(
                        ApplicationProvider.getApplicationContext());
        final Geodetic base =
                Geodetic.fromLatitudeLongitude(
                        new Angle(48.50248706742132, Unit.DEGREES),
                        new Angle(8.992563508173783, Unit.DEGREES));
        final int totalStops = 27;

        // When
        final URL directionsUrlTemplate =
                directionsUrlTemplateFactory.createDirectionsUrlTemplate(
                        base,
                        totalStops);

        // Then
        assertEquals(
                URLs.createUrl("https://www.google.com/maps/dir/Waypoint%201/Waypoint%202/Waypoint%203/Waypoint%204/Waypoint%205/Waypoint%206/Waypoint%207/Waypoint%208/Waypoint%209/Waypoint%2010/Waypoint%2011/Waypoint%2012/Waypoint%2013/Waypoint%2014/Waypoint%2015/Waypoint%2016/Waypoint%2017/Waypoint%2018/Waypoint%2019/Waypoint%2020/Waypoint%2021/Waypoint%2022/Waypoint%2023/Waypoint%2024/Waypoint%2025/Waypoint%2026/Waypoint%2027/data=!3m2!1e3!4b1!4m109!4m108!1m3!2m2!1d8.9925635!2d48.5024871!1m3!2m2!1d8.9928635!2d48.5029871!1m3!2m2!1d8.9931635!2d48.5034871!1m3!2m2!1d8.9934635!2d48.5039871!1m3!2m2!1d8.9937635!2d48.5044871!1m3!2m2!1d8.9940635!2d48.5049871!1m3!2m2!1d8.9943635!2d48.5054871!1m3!2m2!1d8.9946635!2d48.5059871!1m3!2m2!1d8.9949635!2d48.5064871!1m3!2m2!1d8.9952635!2d48.5069871!1m3!2m2!1d8.9955635!2d48.5074871!1m3!2m2!1d8.9958635!2d48.5079871!1m3!2m2!1d8.9961635!2d48.5084871!1m3!2m2!1d8.9964635!2d48.5089871!1m3!2m2!1d8.9967635!2d48.5094871!1m3!2m2!1d8.9970635!2d48.5099871!1m3!2m2!1d8.9973635!2d48.5104871!1m3!2m2!1d8.9976635!2d48.5109871!1m3!2m2!1d8.9979635!2d48.5114871!1m3!2m2!1d8.9982635!2d48.5119871!1m3!2m2!1d8.9985635!2d48.5124871!1m3!2m2!1d8.9988635!2d48.5129871!1m3!2m2!1d8.9991635!2d48.5134871!1m3!2m2!1d8.9994635!2d48.5139871!1m3!2m2!1d8.9997635!2d48.5144871!1m3!2m2!1d9.0000635!2d48.5149871!1m3!2m2!1d9.0003635!2d48.5154871?entry=ttu"),
                directionsUrlTemplate);
    }
}
