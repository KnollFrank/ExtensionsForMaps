package de.KnollFrank.routeoptimizerforgooglemaps.billing;

public interface BillingListener {

    void onSubscriptionStatusChanged(boolean isSubscribed);

    void onProductDetailsLoaded();

    void onBillingError(String message);
}
