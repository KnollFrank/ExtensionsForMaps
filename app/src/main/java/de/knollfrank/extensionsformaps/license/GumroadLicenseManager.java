package de.knollfrank.extensionsformaps.license;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GumroadLicenseManager implements LicenseManager {

    private static final String PREFS_NAME = "license_prefs";
    private static final String KEY_IS_PRO = "is_pro";
    private static final String KEY_LICENSE_KEY = "license_key";
    private static final String GUMROAD_PRODUCT_ID = "-QghnDM3ybPOOTcK762teA==";
    private static final String BASE_URL = "https://api.gumroad.com/";

    private final SharedPreferences prefs;
    private final GumroadService gumroadService;

    public GumroadLicenseManager(Context context) {
        this(context, createDefaultService());
    }

    GumroadLicenseManager(Context context, GumroadService gumroadService) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gumroadService = gumroadService;
    }

    private static GumroadService createDefaultService() {
        return new Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GumroadService.class);
    }

    @Override
    public boolean isPro() {
        return prefs.getBoolean(KEY_IS_PRO, false);
    }

    @Override
    public boolean isProFeatureRequired(int currentStopCount) {
        return currentStopCount > 15;
    }

    @Override
    public CompletableFuture<Boolean> activate(final String licenseKey) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        gumroadService
                .verifyLicense(GUMROAD_PRODUCT_ID, licenseKey)
                .enqueue(
                        new Callback<>() {

                            @Override
                            public void onResponse(Call<GumroadResponse> call, Response<GumroadResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getPurchase().isValid()) {
                                    prefs.edit()
                                            .putBoolean(KEY_IS_PRO, true)
                                            .putString(KEY_LICENSE_KEY, licenseKey)
                                            .apply();
                                    future.complete(true);
                                } else {
                                    future.complete(false);
                                }
                            }

                            @Override
                            public void onFailure(Call<GumroadResponse> call, Throwable t) {
                                future.completeExceptionally(t);
                            }
                        });

        return future;
    }

    @Override
    public CompletableFuture<Void> verifyExistingLicense() {
        String licenseKey = prefs.getString(KEY_LICENSE_KEY, null);
        if (licenseKey == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        gumroadService
                .verifyLicense(GUMROAD_PRODUCT_ID, licenseKey)
                .enqueue(
                        new Callback<>() {

                            @Override
                            public void onResponse(Call<GumroadResponse> call, Response<GumroadResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    boolean isValid = response.body().isSuccess() && response.body().getPurchase().isValid();
                                    if (!isValid) {
                                        // Deactivate Pro if no longer valid
                                        prefs.edit().putBoolean(KEY_IS_PRO, false).apply();
                                    }
                                }
                                future.complete(null);
                            }

                            @Override
                            public void onFailure(Call<GumroadResponse> call, Throwable t) {
                                // If network fails, we don't deactivate - keep last known status
                                future.complete(null);
                            }
                        });
        return future;
    }
}
