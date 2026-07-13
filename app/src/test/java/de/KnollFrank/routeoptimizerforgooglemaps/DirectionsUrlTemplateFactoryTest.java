package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

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
        final Geodetic base =
                Geodetic.fromLatitudeLongitude(
                        new Angle(48.50248706742132, Unit.DEGREES),
                        new Angle(8.992563508173783, Unit.DEGREES));
        final int totalStops = 27;

        // When
        final URL directionsUrlTemplate =
                DirectionsUrlTemplateFactory.createDirectionsUrlTemplate(
                        base,
                        totalStops);

        // Then
        assertEquals(
                URLs.createUrl("https://www.google.com/maps/dir/Wegpunkt%201/Wegpunkt%202/Wegpunkt%203/Wegpunkt%204/Wegpunkt%205/Wegpunkt%206/Wegpunkt%207/Wegpunkt%208/Wegpunkt%209/Wegpunkt%2010/Wegpunkt%2011/Wegpunkt%2012/Wegpunkt%2013/Wegpunkt%2014/Wegpunkt%2015/Wegpunkt%2016/Wegpunkt%2017/Wegpunkt%2018/Wegpunkt%2019/Wegpunkt%2020/Wegpunkt%2021/Wegpunkt%2022/Wegpunkt%2023/Wegpunkt%2024/Wegpunkt%2025/Wegpunkt%2026/Wegpunkt%2027/data=!3m2!1e3!4b1!4m109!4m108!1m3!2m2!1d8.9926!2d48.5025!1m3!2m2!1d8.9929!2d48.5030!1m3!2m2!1d8.9932!2d48.5035!1m3!2m2!1d8.9935!2d48.5040!1m3!2m2!1d8.9938!2d48.5045!1m3!2m2!1d8.9941!2d48.5050!1m3!2m2!1d8.9944!2d48.5055!1m3!2m2!1d8.9947!2d48.5060!1m3!2m2!1d8.9950!2d48.5065!1m3!2m2!1d8.9953!2d48.5070!1m3!2m2!1d8.9956!2d48.5075!1m3!2m2!1d8.9959!2d48.5080!1m3!2m2!1d8.9962!2d48.5085!1m3!2m2!1d8.9965!2d48.5090!1m3!2m2!1d8.9968!2d48.5095!1m3!2m2!1d8.9971!2d48.5100!1m3!2m2!1d8.9974!2d48.5105!1m3!2m2!1d8.9977!2d48.5110!1m3!2m2!1d8.9980!2d48.5115!1m3!2m2!1d8.9983!2d48.5120!1m3!2m2!1d8.9986!2d48.5125!1m3!2m2!1d8.9989!2d48.5130!1m3!2m2!1d8.9992!2d48.5135!1m3!2m2!1d8.9995!2d48.5140!1m3!2m2!1d8.9998!2d48.5145!1m3!2m2!1d9.0001!2d48.5150!1m3!2m2!1d9.0004!2d48.5155?entry=ttu"),
                directionsUrlTemplate);
    }
}