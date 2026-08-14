package de.knollfrank.extensionsformaps.license;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(RobolectricTestRunner.class)
public class GumroadLicenseManagerTest {

    private GumroadLicenseManager licenseManager;
    private GumroadService mockService;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        mockService = mock(GumroadService.class);
        licenseManager = new GumroadLicenseManager(mockService, getLicensePrefs());
    }

    @Test
    public void testProFeatureRequired() {
        assertFalse(licenseManager.isProFeatureRequired(15));
        assertTrue(licenseManager.isProFeatureRequired(16));
    }

    @Test
    public void testActivate_success() {
        // Given
        final String key = "VALID-KEY";
        final Call<GumroadResponse> mockCall = mock(Call.class);
        final GumroadResponse successResponse = mock(GumroadResponse.class);
        configure(successResponse, true);
        when(mockService.verifyLicense(anyString(), anyString())).thenReturn(mockCall);
        call_enqueue_onResponse_gumroadResponse(mockCall, successResponse);

        // When
        final CompletableFuture<Boolean> future = licenseManager.activate(key);

        // Then
        assertTrue(future.join());
        assertTrue(licenseManager.isPro());
    }

    @Test
    public void testActivate_refunded_fails() {
        // Given
        final String key = "REFUNDED-KEY";
        final Call<GumroadResponse> mockCall = mock(Call.class);
        final GumroadResponse successResponse = mock(GumroadResponse.class);
        configure(successResponse, false);
        when(mockService.verifyLicense(anyString(), anyString())).thenReturn(mockCall);
        call_enqueue_onResponse_gumroadResponse(mockCall, successResponse);

        // When
        final CompletableFuture<Boolean> future = licenseManager.activate(key);

        // Then
        assertFalse(future.join());
        assertFalse(licenseManager.isPro());
    }

    @Test
    public void testActivate_failure() {
        // Given
        final String key = "INVALID-KEY";
        final Call<GumroadResponse> mockCall = mock(Call.class);
        final GumroadResponse failureResponse = mock(GumroadResponse.class);
        when(failureResponse.isSuccess()).thenReturn(false);

        when(mockService.verifyLicense(anyString(), anyString())).thenReturn(mockCall);
        call_enqueue_onResponse_gumroadResponse(mockCall, failureResponse);

        // When
        CompletableFuture<Boolean> future = licenseManager.activate(key);

        // Then
        assertFalse(future.join());
        assertFalse(licenseManager.isPro());
    }

    @Test
    public void testVerifyExistingLicense_deactivatesIfRefunded() {
        // Given: Already pro
        getLicensePrefs()
                .edit()
                .putBoolean(GumroadLicenseManager.KEY_IS_PRO, true)
                .putString(GumroadLicenseManager.KEY_LICENSE_KEY, "SOME-KEY")
                .apply();

        final Call<GumroadResponse> mockCall = mock(Call.class);
        final GumroadResponse refundedResponse = mock(GumroadResponse.class);
        configure(refundedResponse, false); // Refunded
        when(mockService.verifyLicense(anyString(), anyString())).thenReturn(mockCall);
        call_enqueue_onResponse_gumroadResponse(mockCall, refundedResponse);

        // When
        licenseManager.verifyExistingLicense().join();

        // Then
        assertFalse(licenseManager.isPro());
    }

    private SharedPreferences getLicensePrefs() {
        return context.getSharedPreferences("license_prefs", Context.MODE_PRIVATE);
    }

    private static void configure(final GumroadResponse successResponse, final boolean isValid) {
        when(successResponse.isSuccess()).thenReturn(true);
        final GumroadResponse.Purchase mockPurchase = mock(GumroadResponse.Purchase.class);
        when(successResponse.getPurchase()).thenReturn(mockPurchase);
        when(mockPurchase.isValid()).thenReturn(isValid); // Valid includes refunded check
    }

    private static void call_enqueue_onResponse_gumroadResponse(final Call<GumroadResponse> call, final GumroadResponse gumroadResponse) {
        Mockito
                .doAnswer(
                        invocation -> {
                            final Callback<GumroadResponse> callback = invocation.getArgument(0);
                            callback.onResponse(call, Response.success(gumroadResponse));
                            return null;
                        })
                .when(call).enqueue(any());
    }
}
