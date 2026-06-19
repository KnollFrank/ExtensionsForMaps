package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

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

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * End-to-end integration test that automates Google Maps to verify the full optimization cycle.
 * Waits for background processing to complete and verifies the final optimized route display
 * by checking for the "Preview" button on the bottom sheet.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 26)
@LargeTest
public class GoogleMapsIntegrationTest {

	private UiDevice device;
	private static final int TIMEOUT = 15000;
	private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
	private static final String APP_PACKAGE = "de.KnollFrank.routeoptimizerforgooglemaps";
	private static final String TAG = "GoogleMapsTest";

	@Before
	public void setUp() {
		device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
		device.pressHome();
	}

	@Test
	public void testFullOptimizationCycleFromGoogleMaps() {
		// 1. Launch Google Maps directly with the pre-defined Route-URL
		final Context context = ApplicationProvider.getApplicationContext();

		final String routeUrl = "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0&g_ep=CAESCDI2LjE2LjEyGAAgkUEqiwEsOTQyNjc3MjcsOTQyOTIxOTUsOTQyOTk1MzIsMTAwNzk2NDk4LDEwMDc5Nzc2MSwxMDA3OTY1MzUsOTQyODA1NzYsMTAwODExOTYwLDk0MjA3Mzk0LDk0MjA3NTA2LDk0MjA4NTA2LDk0MjE4NjUzLDk0MjI5ODM5LDk0Mjc1MTY4LDk0Mjc5NjE5QgJVUw%3D%3D&skid=0a1f62d3-c01c-47b9-b4b6-ccadc456baa8";
		final Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(routeUrl));
		mapsIntent.setPackage(MAPS_PACKAGE);
		mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		context.startActivity(mapsIntent);

		device.wait(Until.hasObject(By.pkg(MAPS_PACKAGE).depth(0)), TIMEOUT);
		device.waitForIdle();

		UiObject2 gotIt = device.findObject(By.text(Pattern.compile("Got it|Verstanden", Pattern.CASE_INSENSITIVE)));
		if (gotIt != null) gotIt.click();

		// 2. Trigger "Share directions" directly from the Bottom-Sheet
		Pattern sharePattern = Pattern.compile("Share|Share directions|Teilen|Wegbeschreibung teilen", Pattern.CASE_INSENSITIVE);

		UiObject2 shareBtn = device.wait(Until.findObject(By.desc(sharePattern)), TIMEOUT * 2);
		if (shareBtn == null) {
			shareBtn = device.wait(Until.findObject(By.text(sharePattern)), TIMEOUT);
		}

		if (shareBtn == null) {
			dumpWindowHierarchy();
			fail("Konnte den 'Share' Button auf dem Bottom-Sheet nicht finden. UI Dump gespeichert.");
		}
		shareBtn.click();

		// 3. Select "Routeoptimizer" from Android Share Sheet via manual swiping
		device.waitForIdle();
		boolean appFound = false;

		for (int i = 0; i < 7; i++) {
			UiObject2 appInShareSheet = device.findObject(By.textContains("Routeopt"));

			if (appInShareSheet != null) {
				appInShareSheet.click();
				appFound = true;
				break;
			}

			int startX = device.getDisplayWidth() / 2;
			int startY = (int) (device.getDisplayHeight() * 0.8);
			int endY = (int) (device.getDisplayHeight() * 0.3);

			device.swipe(startX, startY, startX, endY, 20);
			device.waitForIdle();
		}

		if (!appFound) {
			dumpWindowHierarchy();
			fail("Routeoptimizer wurde im Teilen-Menü auch nach 7 manuellen Swipes nicht gefunden. UI Dump gespeichert.");
		}

		// =====================================================================
		// 4. Warten auf Fertigstellung der Optimierung & Verifikation (Preview)
		// =====================================================================

		// Erhöhtes Timeout für die Hintergrundberechnung (UrlExpander + Geocoder + Jsprit)
		boolean backInMaps = device.wait(Until.hasObject(By.pkg(MAPS_PACKAGE)), TIMEOUT * 3);
		assertTrue("Rückkehr zu Google Maps nach der Optimierung fehlgeschlagen", backInMaps);

		// Sicherstellen, dass die transparente Routeoptimizer-Activity ordnungsgemäß schließt
		boolean appGone = device.wait(Until.gone(By.pkg(APP_PACKAGE)), TIMEOUT);
		assertTrue("Routeoptimizer Activity wurde nach dem Prozess nicht beendet", appGone);

		// Überprüfung, ob die neue Route geladen wurde.
		// Da kein "Start"-Button angezeigt wird, prüfen wir auf das Vorhandensein des "Preview"-Buttons.
		Pattern previewButtonPattern = Pattern.compile("^Preview$|^Preview route$", Pattern.CASE_INSENSITIVE);

		// Bis zu 30 Sekunden Geduld für das Rendern der geänderten Stopps durch Maps
		UiObject2 previewBtn = device.wait(Until.findObject(By.text(previewButtonPattern)), TIMEOUT * 2);
		if (previewBtn == null) {
			previewBtn = device.findObject(By.desc(previewButtonPattern));
		}

		// Absicherung: Wenn der "Preview"-Button existiert, steht das korrekte Bottom-Sheet bereit
		if (previewBtn == null) {
			dumpWindowHierarchy();
			fail("Die optimierte Route wurde nicht in Google Maps geladen (Kein 'Preview'-Button auf dem Bildschirm gefunden). UI Dump gespeichert.");
		}
	}

	private void dumpWindowHierarchy() {
		try {
			File dumpFile = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null), "ui_dump.xml");
			device.dumpWindowHierarchy(dumpFile);
			Log.e(TAG, "==================================================");
			Log.e(TAG, "UI DUMP SAVED TO: " + dumpFile.getAbsolutePath());
			Log.e(TAG, "==================================================");
		} catch (IOException e) {
			Log.e(TAG, "Failed to dump window hierarchy", e);
		}
	}
}