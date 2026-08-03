package de.knollfrank.extensionsformaps.license;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LocalLicenseManagerTest {

    private LocalLicenseManager licenseManager;

    @Before
    public void setUp() {
        licenseManager = new LocalLicenseManager(ApplicationProvider.getApplicationContext());
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
        assertFalse(licenseManager.activate("WRONG-KEY").join());
        assertFalse(licenseManager.isPro());
    }

    @Test
    public void testActivation_withCorrectKey_succeeds() {
        // The fixed key is "PRO"
        assertTrue(licenseManager.activate("PRO").join());
        assertTrue(licenseManager.isPro());
    }
}
