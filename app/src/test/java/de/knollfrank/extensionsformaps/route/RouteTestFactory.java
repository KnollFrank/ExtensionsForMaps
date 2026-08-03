package de.knollfrank.extensionsformaps.route;

import static de.knollfrank.extensionsformaps.coordinate.Unit.DEGREES;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;

class RouteTestFactory {

    public static Route createRouteWithTwoWaypoints() {
        return new Route(
                new Stop(
                        "1",
                        "Origin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(10.0, DEGREES),
                                new Angle(20.0, DEGREES)),
                        Optional.empty()),
                List.of(
                        new Stop(
                                "2",
                                "Waypoint",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(15.0, DEGREES),
                                        new Angle(25.0, DEGREES)),
                                Optional.empty())),
                new Stop(
                        "3",
                        "Destination",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(30.0, DEGREES),
                                new Angle(40.0, DEGREES)),
                        Optional.empty()));
    }
}
