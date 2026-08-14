package de.knollfrank.extensionsformaps.license;

import com.google.gson.annotations.SerializedName;

class GumroadResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("purchase")
    private Purchase purchase;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public static class Purchase {

        @SerializedName("refunded")
        private boolean refunded;

        @SerializedName("disputed")
        private boolean disputed;

        @SerializedName("chargebacked")
        private boolean chargebacked;

        public boolean isRefunded() {
            return refunded;
        }

        public boolean isDisputed() {
            return disputed;
        }

        public boolean isChargebacked() {
            return chargebacked;
        }

        public boolean isValid() {
            return !refunded && !disputed && !chargebacked;
        }
    }
}
