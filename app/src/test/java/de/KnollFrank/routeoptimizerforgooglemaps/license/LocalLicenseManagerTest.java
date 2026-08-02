package de.KnollFrank.routeoptimizerforgooglemaps.license;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LocalLicenseManagerTest {

    private LocalLicenseManager licenseManager;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        licenseManager = new LocalLicenseManager(context);
    }

    @Test
    public void testInitialState_isNotPro() {
        assertFalse(licenseManager.isPro());
    }

    @Test
    public void testProFeatureRequired_logic() {
        assertFalse("15 stops should be allowed in free", licenseManager.isProFeatureRequired(15));
        assertTrue("16 stops should require Pro", licenseManager.isProFeatureRequired(16));
    }

    @Test
    public void testActivation_withWrongKey_fails() {
        assertFalse(licenseManager.activate("WRONG-KEY"));
        assertFalse(licenseManager.isPro());
    }

    @Test
    public void testActivation_withCorrectKey_succeeds() {
        // We can't easily get the testKey because it's private, 
        // but we can check if the one from preferences works if we could access it.
        // For testing purposes, we can trust the log or reflect if needed, 
        // but let's test the mechanism by checking if it persists.
        
        // Actually, let's just test that after activation it returns true.
        // We can use a trick: since it's Robolectric, we can check the prefs directly.
        String key = context.getSharedPreferences("license_prefs", Context.MODE_PRIVATE).getString("test_key", "");
        assertFalse(key.isEmpty());
        
        assertTrue(licenseManager.activate(key));
        assertTrue(licenseManager.isPro());
    }
}
