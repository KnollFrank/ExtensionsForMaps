package de.knollfrank.extensionsformaps.feature;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Dialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;

import java.util.Collections;

import de.knollfrank.extensionsformaps.license.LicenseManager;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class AddStopActivityLicenseTest {

    private LicenseManager mockLicenseManager;

    @Before
    public void setUp() {
        mockLicenseManager = mock(LicenseManager.class);
        LicenseManagerProvider.setInstance(mockLicenseManager);
    }

    @Test
    public void testHandleRoute_with15Stops_noPro_showsUpgradeDialog() {
        // Given
        when(mockLicenseManager.isPro()).thenReturn(false);
        
        ActivityController<AddStopActivity> controller = Robolectric.buildActivity(AddStopActivity.class);
        AddStopActivity activity = controller.setup().get();
        
        Route mockRoute = mock(Route.class);
        Stop mockStop = mock(Stop.class);
        when(mockRoute.stops()).thenReturn(Collections.nCopies(15, mockStop));

        // When
        try {
            java.lang.reflect.Method method = AddStopActivity.class.getDeclaredMethod("handleRoute", Route.class);
            method.setAccessible(true);
            method.invoke(activity, mockRoute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Then
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        // We can't easily cast to ShadowAlertDialog if it's an androidx one without extra deps, 
        // but we can check if it's showing.
        assertTrue(dialog.isShowing());
    }

    @Test
    public void testHandleRoute_with15Stops_isPro_doesNotShowUpgradeDialog() {
        // Given
        when(mockLicenseManager.isPro()).thenReturn(true);
        
        ActivityController<AddStopActivity> controller = Robolectric.buildActivity(AddStopActivity.class);
        AddStopActivity activity = controller.setup().get();
        
        Route mockRoute = mock(Route.class);
        Stop mockStop = mock(Stop.class);
        when(mockRoute.stops()).thenReturn(Collections.nCopies(15, mockStop));

        // When
        try {
            java.lang.reflect.Method method = AddStopActivity.class.getDeclaredMethod("handleRoute", Route.class);
            method.setAccessible(true);
            method.invoke(activity, mockRoute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Then
        Dialog dialog = ShadowDialog.getLatestDialog();
        // It might be null or not an upgrade dialog. 
        // In our case, addStopAndFinish shows a Toast, not a dialog.
        if (dialog != null) {
             assertTrue(!dialog.isShowing());
        }
    }
}
