package de.knollfrank.extensionsformaps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;

import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrlFactory;

@RunWith(RobolectricTestRunner.class)
// FK-TODO: macht das Sinn?
public class RouteOptimizationWorkflowTest {

    @Test
    public void testOptimizeThenShowRoute_callsOrchestrator() throws MalformedURLException {
        // Given
        final RouteOptimizationOrchestrator orchestrator = mock(RouteOptimizationOrchestrator.class);
        final ProgressOverlay progressOverlay = mock(ProgressOverlay.class);
        final RouteOptimizationWorkflow workflow = new RouteOptimizationWorkflow(orchestrator, progressOverlay);
        final DirectionsUrl directionsUrl =
                DirectionsUrlFactory
                        .createDirectionsUrl(new URL("https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0&g_ep=CAESCDI2LjE2LjEyGAAgkUEqiwEsOTQyNjc3MjcsOTQyOTIxOTUsOTQyOTk1MzIsMTAwNzk2NDk4LDEwMDc5Nzc2MSwxMDA3OTY1MzUsOTQyODA1NzYsMTAwODExOTYwLDk0MjA3Mzk0LDk0MjA3NTA2LDk0MjA4NTA2LDk0MjE4NjUzLDk0MjI5ODM5LDk0Mjc1MTY4LDk0Mjc5NjE5QgJVUw%3D%3D&skid=0a1f62d3-c01c-47b9-b4b6-ccadc456baa8"))
                        .orElseThrow();

        // When
        workflow.optimizeThenShowRoute(directionsUrl);

        // Then
        verify(orchestrator).extractRouteFromDirectionsUrl(directionsUrl);
    }
}
