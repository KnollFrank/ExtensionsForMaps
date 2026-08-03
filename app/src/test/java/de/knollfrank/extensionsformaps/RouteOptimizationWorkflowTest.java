package de.knollfrank.extensionsformaps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(RobolectricTestRunner.class)
// FK-TODO: macht das Sinn?
public class RouteOptimizationWorkflowTest {

    @Test
    public void testOptimizeThenShowRoute_callsOrchestrator() throws MalformedURLException {
        // Given
        final RouteOptimizationOrchestrator orchestrator = mock(RouteOptimizationOrchestrator.class);
        final ProgressOverlay progressOverlay = mock(ProgressOverlay.class);
        final RouteOptimizationWorkflow workflow = new RouteOptimizationWorkflow(orchestrator, progressOverlay);
        final URL url = new URL("https://example.com");

        // When
        workflow.optimizeThenShowRoute(url);

        // Then
        verify(orchestrator).extractRouteFromDirectionsUrl(url);
    }
}
