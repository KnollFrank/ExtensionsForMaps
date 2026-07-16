package de.KnollFrank.routeoptimizerforgooglemaps.billing;

import android.app.Activity;
import android.util.Log;

public class DebugBillingProvider implements BillingProvider {

    private static final String TAG = "DebugBillingProvider";
    private boolean isSubscribed = false;
    // FK-TODO: make Optional<BillingListener>
    private BillingListener listener;

    @Override
    public void startConnection() {
        Log.d(TAG, "Simulating billing connection established.");
        if (listener != null) {
            listener.onSubscriptionStatusChanged(isSubscribed);
        }
    }

    @Override
    public void endConnection() {
        Log.d(TAG, "Simulating billing connection ended.");
    }

    @Override
    public void launchSubscriptionFlow(final Activity activity) {
        Log.d(TAG, "Simulating subscription flow launched.");
        // Simulate a successful purchase immediately
        isSubscribed = true;
        if (listener != null) {
            listener.onSubscriptionStatusChanged(isSubscribed);
        }
    }

    @Override
    public boolean isSubscribed() {
        return isSubscribed;
    }

    @Override
    public void setListener(final BillingListener listener) {
        this.listener = listener;
    }

    // Extra method for testing to toggle status
    public void setSubscribed(final boolean subscribed) {
        this.isSubscribed = subscribed;
        if (listener != null) {
            listener.onSubscriptionStatusChanged(isSubscribed);
        }
    }
}
