package de.KnollFrank.routeoptimizerforgooglemaps.billing;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GooglePlayBillingProvider implements BillingProvider, PurchasesUpdatedListener {

    private static final String TAG = "GooglePlayBilling";
    private static final String PRODUCT_ID_MONTHLY = "premium_stops_monthly";

    private final BillingClient billingClient;
    private final Map<String, ProductDetails> productDetailsMap = new HashMap<>();
    // FK-TODO: make Optional<BillingListener>
    private BillingListener listener;
    private boolean isSubscribed = false;

    public GooglePlayBillingProvider(final Context context) {
        billingClient =
                BillingClient
                        .newBuilder(context)
                        .setListener(this)
                        .enablePendingPurchases(
                                PendingPurchasesParams
                                        .newBuilder()
                                        .enableOneTimeProducts()
                                        .build())
                        .build();
    }

    @Override
    public void setListener(final BillingListener listener) {
        this.listener = listener;
    }

    @Override
    public void startConnection() {
        billingClient.startConnection(
                new BillingClientStateListener() {

                    @Override
                    public void onBillingSetupFinished(@NonNull final BillingResult billingResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            queryPurchases();
                            queryProductDetails();
                        } else if (listener != null) {
                            listener.onBillingError(billingResult.getDebugMessage());
                        }
                    }

                    @Override
                    public void onBillingServiceDisconnected() {
                        // Handle reconnection if needed
                    }
                });
    }

    @Override
    public void endConnection() {
        if (billingClient.isReady()) {
            billingClient.endConnection();
        }
    }

    @Override
    public void launchSubscriptionFlow(final Activity activity) {
        final ProductDetails productDetails = productDetailsMap.get(PRODUCT_ID_MONTHLY);
        if (productDetails == null || productDetails.getSubscriptionOfferDetails() == null || productDetails.getSubscriptionOfferDetails().isEmpty()) {
            if (listener != null) {
                listener.onBillingError("Product not found or offer details missing.");
            }
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
                                                .setOfferToken(getFirstOfferToken(productDetails))
                                                .build()))
                        .build());
    }

    @Override
    public boolean isSubscribed() {
        return isSubscribed;
    }

    @Override
    public void onPurchasesUpdated(@NonNull final BillingResult billingResult,
                                   @Nullable final List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (final Purchase purchase : purchases) {
                if (purchase.getProducts().contains(PRODUCT_ID_MONTHLY) &&
                        purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    isSubscribed = true;
                    if (listener != null) {
                        listener.onSubscriptionStatusChanged(true);
                    }
                    return;
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled purchase");
        } else if (listener != null) {
            listener.onBillingError(billingResult.getDebugMessage());
        }
    }

    private void queryPurchases() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams
                        .newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                new PurchasesResponseListener() {

                    @Override
                    public void onQueryPurchasesResponse(@NonNull final BillingResult billingResult,
                                                         @NonNull final List<Purchase> purchases) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            isSubscribed = false;
                            for (final Purchase purchase : purchases) {
                                if (purchase.getProducts().contains(PRODUCT_ID_MONTHLY) &&
                                        purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    isSubscribed = true;
                                    break;
                                }
                            }
                            if (listener != null) {
                                listener.onSubscriptionStatusChanged(isSubscribed);
                            }
                        }
                    }
                });
    }

    private void queryProductDetails() {
        billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams
                        .newBuilder()
                        .setProductList(
                                List.of(
                                        QueryProductDetailsParams.Product
                                                .newBuilder()
                                                .setProductId(PRODUCT_ID_MONTHLY)
                                                .setProductType(BillingClient.ProductType.SUBS)
                                                .build()))
                        .build(),
                new ProductDetailsResponseListener() {

                    @Override
                    public void onProductDetailsResponse(@NonNull final BillingResult billingResult,
                                                         @NonNull final QueryProductDetailsResult productDetailsResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            final List<ProductDetails> detailsList = productDetailsResult.getProductDetailsList();
                            for (final ProductDetails details : detailsList) {
                                productDetailsMap.put(details.getProductId(), details);
                            }
                        }
                    }
                });
    }

    // For simplicity, select the first offer/base plan
    private static String getFirstOfferToken(final ProductDetails productDetails) {
        return productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
    }
}
