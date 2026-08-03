package de.knollfrank.extensionsformaps.license;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
        licenseManager = new GumroadLicenseManager(context, mockService);
    }

    @Test
    public void testProFeatureRequired() {
        assertFalse(licenseManager.isProFeatureRequired(15));
        assertTrue(licenseManager.isProFeatureRequired(16));
    }

    @Test
    public void testActivate_success() {
        // Given
        String key = "VALID-KEY";
        Call<GumroadResponse> mockCall = mock(Call.class);
        GumroadResponse successResponse = mock(GumroadResponse.class);
        GumroadResponse.Purchase mockPurchase = mock(GumroadResponse.Purchase.class);
        
        when(successResponse.isSuccess()).thenReturn(true);
        when(successResponse.getPurchase()).thenReturn(mockPurchase);
        when(mockPurchase.isValid()).thenReturn(true);

        when(mockService.verifyLicense(anyString(), anyString())).thenReturn(mockCall);
        doAnswer(invocation -> {
            Callback<GumroadResponse> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.success(successResponse));
            return null;
        }).when(mockCall).enqueue(any());

        // When
        CompletableFuture<Boolean> future = licenseManager.activate(key);

        // Then
        assertTrue(future.join());
        assertTrue(licenseManager.isPro());
    }

    @Test
    public void testActivate_refunded_fails() {
        // Given
        String key = "REFUNDED-KEY";
        Call<GumroadResponse> mockCall = mock(Call.class);
        GumroadResponse successResponse = mock(GumroadResponse.class);
        GumroadResponse.Purchase mockPurchase = mock(GumroadResponse.Purchase.class);
        
        when(successResponse.isSuccess()).thenReturn(true);
        when(successResponse.getPurchase()).thenReturn(mockPurchase);
        when(mockPurchase.isValid()).thenReturn(false); // Valid includes refunded check

        when(mockService.verifyLicense(anyString(), anyString())).thenReturn(mockCall);
        doAnswer(invocation -> {
            Callback<GumroadResponse> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.success(successResponse));
            return null;
        }).when(mockCall).enqueue(any());

        // When
        CompletableFuture<Boolean> future = licenseManager.activate(key);

        // Then
        assertFalse(future.join());
        assertFalse(licenseManager.isPro());
    }

    @Test
    public void testActivate_failure() {
        // Given
        String key = "INVALID-KEY";
        Call<GumroadResponse> mockCall = mock(Call.class);
        GumroadResponse failureResponse = mock(GumroadResponse.class);
        when(failureResponse.isSuccess()).thenReturn(false);

        when(mockService.verifyLicense(anyString(), anyString())).thenReturn(mockCall);
        doAnswer(invocation -> {
            Callback<GumroadResponse> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.success(failureResponse));
            return null;
        }).when(mockCall).enqueue(any());

        // When
        CompletableFuture<Boolean> future = licenseManager.activate(key);

        // Then
        assertFalse(future.join());
        assertFalse(licenseManager.isPro());
    }

    @Test
    public void testVerifyExistingLicense_deactivatesIfRefunded() {
        // Given: Already pro
        context.getSharedPreferences("license_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("is_pro", true)
                .putString("license_key", "SOME-KEY")
                .apply();

        Call<GumroadResponse> mockCall = mock(Call.class);
        GumroadResponse refundedResponse = mock(GumroadResponse.class);
        GumroadResponse.Purchase mockPurchase = mock(GumroadResponse.Purchase.class);
        
        when(refundedResponse.isSuccess()).thenReturn(true);
        when(refundedResponse.getPurchase()).thenReturn(mockPurchase);
        when(mockPurchase.isValid()).thenReturn(false); // Refunded

        when(mockService.verifyLicense(anyString(), anyString())).thenReturn(mockCall);
        doAnswer(invocation -> {
            Callback<GumroadResponse> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.success(refundedResponse));
            return null;
        }).when(mockCall).enqueue(any());

        // When
        licenseManager.verifyExistingLicense().join();

        // Then
        assertFalse(licenseManager.isPro());
    }
}
