package de.knollfrank.extensionsformaps;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.view.ContextThemeWrapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowDialog;

import java.util.Collections;
import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.coordinate.Unit;
import de.knollfrank.extensionsformaps.license.LicenseManager;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizer;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class RouteOptimizationWorkflowLicenseTest {

    private LicenseManager mockLicenseManager;
    private RouteOptimizer mockRouteOptimizer;

    @Before
    public void setUp() {
        mockLicenseManager = mock(LicenseManager.class);
        LicenseManagerProvider.setInstance(mockLicenseManager);
        mockRouteOptimizer = mock(RouteOptimizer.class);
    }

    @Test
    public void testOnExtractRouteFromDirectionsUrlSuccess_with16Stops_noPro_showsUpgradeDialog() {
        // Given
        when(mockLicenseManager.isPro()).thenReturn(false);
        when(mockLicenseManager.isProFeatureRequired(16)).thenReturn(true);
        
        Context context = new ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_ExtensionsForMaps);
        RouteOptimizationWorkflow workflow = new RouteOptimizationWorkflow(mockRouteOptimizer, context);
        
        Stop stop = new Stop("1", "Addr", Optional.empty(), 
            Geodetic.fromLatitudeLongitude(new Angle(0, Unit.DEGREES), new Angle(0, Unit.DEGREES)), 
            Optional.empty());
        
        Route route = new Route(stop, Collections.nCopies(14, stop), stop);

        // When
        try {
            java.lang.reflect.Method method = RouteOptimizationWorkflow.class.getDeclaredMethod("createCallback", android.content.Context.class, ProgressOverlay.class);
            method.setAccessible(true);
            RouteOptimizationOrchestrator.Callback callback = (RouteOptimizationOrchestrator.Callback) method.invoke(workflow, context, mock(ProgressOverlay.class));
            callback.onExtractRouteFromDirectionsUrlSuccess(route);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Then
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
    }
}
