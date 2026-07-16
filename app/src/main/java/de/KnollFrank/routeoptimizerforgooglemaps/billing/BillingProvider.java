package de.KnollFrank.routeoptimizerforgooglemaps.billing;

import android.app.Activity;

public interface BillingProvider {

    void startConnection();

    void endConnection();

    void launchSubscriptionFlow(Activity activity);

    boolean isSubscribed();

    String getFormattedPrice();

    void setListener(BillingListener listener);
}
