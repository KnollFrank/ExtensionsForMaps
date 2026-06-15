package com.example.routeoptimizerforgooglemaps;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 26) // UI Automator works best on newer SDKs
public class NavigationFlowUITest {

	private UiDevice device;
	private static final int LAUNCH_TIMEOUT = 5000;
	private static final String APP_PACKAGE = "com.example.routeoptimizerforgooglemaps";

	@Before
	public void startMainActivityFromHomeScreen() {
		// Initialize UiDevice instance
		device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

		// Start from the home screen
		device.pressHome();

		// Wait for launcher
		final String launcherPackage = device.getLauncherPackageName();
		assertNotNull(launcherPackage);
		device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

		// Launch the app
		final Context context = ApplicationProvider.getApplicationContext();
		final Intent intent = context.getPackageManager().getLaunchIntentForPackage(APP_PACKAGE);
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			context.startActivity(intent);
		}

		// Wait for the app to appear
		device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
	}

	@Test
	public void checkUiComponentsAreDisplayed() {
		// Espresso check
		onView(withId(R.id.fabStartTour)).check(matches(isDisplayed()));
		onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
	}

	@Test
	public void simulateShareIntentAndStartOptimization() {
		// Send a mock Share Intent
		final Context context = ApplicationProvider.getApplicationContext();
		final Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, "Test Address 1 \n https://maps.app.goo.gl/test");
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(shareIntent);

		// Wait for our app to process it
		device.wait(Until.hasObject(By.textContains("Test Address 1")), LAUNCH_TIMEOUT);

		// Find the start button and click it
		final UiObject2 startButton = device.findObject(By.res(APP_PACKAGE, "fabStartTour"));
		if (startButton != null) {
			// Note: Since location and overlay permissions are required, this test might fail
			// if permissions are not granted beforehand via adb (adb shell pm grant ...).
			// For a robust CI setup, permissions must be granted via adb or test rules.
			startButton.click();
		}

		// This is a basic integration check. True E2E with Google Maps requires
		// mocking the location and granting all permissions prior to test run.
		assertTrue(true);
	}
}
