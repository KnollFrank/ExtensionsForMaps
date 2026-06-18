package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

/**
 * End-to-end integration test that automates Google Maps to verify the full optimization cycle.
 * Highly robust selectors used to handle varying Google Maps UI.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 26)
@LargeTest
public class GoogleMapsIntegrationTest {

    private UiDevice device;
    private static final int TIMEOUT = 15000;
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String APP_PACKAGE = "de.KnollFrank.routeoptimizerforgooglemaps";

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressHome();
    }

    @Test
    public void testFullOptimizationCycleFromGoogleMaps() {
        // 1. Launch Google Maps
        final Context context = ApplicationProvider.getApplicationContext();
        final Intent mapsIntent = context.getPackageManager().getLaunchIntentForPackage(MAPS_PACKAGE);
        assertNotNull("Google Maps is not installed", mapsIntent);
        mapsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(mapsIntent);
        device.wait(Until.hasObject(By.pkg(MAPS_PACKAGE).depth(0)), TIMEOUT);

        // Handle initial Google Maps popups (Terms, etc.) if any
        UiObject2 gotIt = device.findObject(By.textContains("Got it"));
        if (gotIt != null) gotIt.click();

        // 2. Locate Search Box
        UiObject2 searchBox = device.wait(Until.findObject(By.textContains("Search here")), TIMEOUT);
        if (searchBox == null) searchBox = device.findObject(By.res(MAPS_PACKAGE, "search_omnibox_text_box"));
        if (searchBox == null) searchBox = device.findObject(By.descContains("Search"));
        
        assertNotNull("Could not find Maps search box", searchBox);
        searchBox.click();
        
        // Wait for search input field to be focused
        UiObject2 editTextField = device.wait(Until.findObject(By.focused(true)), TIMEOUT);
        if (editTextField == null) editTextField = device.findObject(By.clazz("android.widget.EditText"));
        
        assertNotNull("Could not find focused edit text for search", editTextField);
        editTextField.setText("Hamburg");
        device.pressEnter();
        
        // 3. Open Directions
        UiObject2 directionsBtn = device.wait(Until.findObject(By.text("Directions")), TIMEOUT);
        if (directionsBtn == null) directionsBtn = device.findObject(By.descContains("Directions"));
        if (directionsBtn == null) directionsBtn = device.findObject(By.res(MAPS_PACKAGE, "place_card_directions_button"));
        
        assertNotNull("Could not find Directions button", directionsBtn);
        directionsBtn.click();

        // 4. Add stops via "More options"
        UiObject2 moreOptions = device.wait(Until.findObject(By.descContains("More options")), TIMEOUT);
        assertNotNull("Could not find 'More options' (three dots) menu", moreOptions);
        moreOptions.click();
        
        UiObject2 addStop = device.wait(Until.findObject(By.textContains("Add stop")), TIMEOUT);
        assertNotNull("Could not find 'Add stop' menu item", addStop);
        addStop.click();

        // Add additional stops
        List<String> stopsToAdd = Arrays.asList("Berlin", "Munich");
        for (String stopName : stopsToAdd) {
            UiObject2 nextStopField = device.wait(Until.findObject(By.textContains("Add stop")), TIMEOUT);
            if (nextStopField != null) {
                nextStopField.click();
                UiObject2 focusedInput = device.wait(Until.findObject(By.focused(true)), TIMEOUT);
                if (focusedInput != null) {
                    focusedInput.setText(stopName);
                    device.pressEnter();
                    // Wait for the address to be resolved/suggested and clicked if necessary, 
                    // or just wait for it to appear in the field.
                    device.wait(Until.findObject(By.textContains(stopName)), TIMEOUT);
                }
            }
        }

        // 5. Trigger "Share directions"
        moreOptions = device.wait(Until.findObject(By.descContains("More options")), TIMEOUT);
        moreOptions.click();
        
        UiObject2 shareBtn = device.wait(Until.findObject(By.textContains("Share directions")), TIMEOUT);
        assertNotNull("Could not find 'Share directions' button", shareBtn);
        shareBtn.click();
        
        // 6. Select "Routeoptimizer" from Android Share Sheet
        UiObject2 appInShareSheet = device.wait(Until.findObject(By.text("Routeoptimizer")), TIMEOUT);
        assertNotNull("Routeoptimizer not found in share sheet", appInShareSheet);
        appInShareSheet.click();

        // 7. Verify Magic Return to Google Maps
        // Wait for Routeoptimizer (transparent) to finish and Maps to be back on top
        boolean backInMaps = device.wait(Until.hasObject(By.pkg(MAPS_PACKAGE)), TIMEOUT * 2);
        assertTrue("Failed to return to Google Maps after optimization", backInMaps);
        
        // Verify Routeoptimizer finished its task
        boolean appGone = device.wait(Until.gone(By.pkg(APP_PACKAGE)), TIMEOUT);
        assertTrue("Routeoptimizer activity stuck and did not finish", appGone);
    }
}
