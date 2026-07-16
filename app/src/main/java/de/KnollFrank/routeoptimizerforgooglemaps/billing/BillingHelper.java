package de.KnollFrank.routeoptimizerforgooglemaps.billing;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingHelper implements PurchasesUpdatedListener {

    private static final String TAG = "BillingHelper";

    // Product IDs
    public static final String COFFEE_ESPRESSO = "coffee_espresso";
    public static final String COFFEE_CAPPUCCINO = "coffee_cappuccino";
    public static final String COFFEE_CAKE = "coffee_cake";

    private final BillingClient billingClient;
    private final Map<String, ProductDetails> productDetailsMap = new HashMap<>();
    private final BillingListener listener;

    public interface BillingListener {

        void onDonationSuccessful();

        void onBillingError(String message);
    }

    public BillingHelper(final Activity activity, final BillingListener listener) {
        this.listener = listener;
        this.billingClient =
                BillingClient
                        .newBuilder(activity)
                        .setListener(this)
                        .enablePendingPurchases(
                                PendingPurchasesParams
                                        .newBuilder()
                                        .enableOneTimeProducts()
                                        .build())
                        .build();
        startConnection();
    }

    public void launchBillingFlow(final Activity activity, final String productId) {
        final ProductDetails productDetails = productDetailsMap.get(productId);
        if (productDetails == null) {
            listener.onBillingError("Product not found or not loaded yet.");
            return;
        }
        billingClient.launchBillingFlow(
                activity,
                BillingFlowParams
                        .newBuilder()
                        .setProductDetailsParamsList(
                                List.of(
                                        BillingFlowParams.ProductDetailsParams
                                                .newBuilder()
                                                .setProductDetails(productDetails)
                                                .build()))
                        .build());
    }

    @Override
    public void onPurchasesUpdated(@NonNull final BillingResult billingResult,
                                   @Nullable final List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (final Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled the purchase.");
        } else {
            listener.onBillingError(billingResult.getDebugMessage());
        }
    }

    public void endConnection() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
        }
    }

    private void startConnection() {
        billingClient.startConnection(
                new BillingClientStateListener() {

                    @Override
                    public void onBillingSetupFinished(@NonNull final BillingResult billingResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            queryProducts();
                        } else {
                            Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                        }
                    }

                    @Override
                    public void onBillingServiceDisconnected() {
                        // Connection lost. Retrying logic could be added here.
                    }
                });
    }

    private void queryProducts() {
        billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams
                        .newBuilder()
                        .setProductList(
                                List.of(
                                        QueryProductDetailsParams.Product
                                                .newBuilder()
                                                .setProductId(COFFEE_ESPRESSO)
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product
                                                .newBuilder()
                                                .setProductId(COFFEE_CAPPUCCINO)
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product
                                                .newBuilder()
                                                .setProductId(COFFEE_CAKE)
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build()))
                        .build(),
                new ProductDetailsResponseListener() {

                    @Override
                    public void onProductDetailsResponse(
                            @NonNull final BillingResult billingResult,
                            @NonNull final QueryProductDetailsResult productDetailsResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            final List<ProductDetails> detailsList = productDetailsResult.getProductDetailsList();
                            for (final ProductDetails productDetails : detailsList) {
                                productDetailsMap.put(productDetails.getProductId(), productDetails);
                            }
                        } else {
                            Log.e(TAG, "Query products failed: " + billingResult.getDebugMessage());
                        }
                    }
                });
    }

    private void handlePurchase(final Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Consumables MUST be consumed immediately to be buyable again
            consumePurchase(purchase);
        }
    }

    private void consumePurchase(final Purchase purchase) {
        billingClient.consumeAsync(
                ConsumeParams
                        .newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build(),
                new ConsumeResponseListener() {

                    @Override
                    public void onConsumeResponse(@NonNull final BillingResult billingResult,
                                                  @NonNull final String purchaseToken) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            Log.i(TAG, "Consumption successful.");
                            listener.onDonationSuccessful();
                        } else {
                            Log.e(TAG, "Consumption failed: " + billingResult.getDebugMessage());
                        }
                    }
                });
    }
}
