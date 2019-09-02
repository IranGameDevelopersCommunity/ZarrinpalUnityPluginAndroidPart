package com.zarinpal.ewallets.purchase;

import org.json.JSONException;
import org.json.JSONObject;

class VerificationPayment {
    private long amount;
    private String authority;
    private String merchantID;

    VerificationPayment() {
    }

    public long getAmount() {
        return this.amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getAuthority() {
        return this.authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getMerchantID() {
        return this.merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public JSONObject getVerificationPaymentAsJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(Payment.AUTHORITY_PARAMS, getAuthority());
        object.put(Payment.AMOUNT_PARAMS, getAmount());
        object.put(Payment.MERCHANT_ID_PARAMS, getMerchantID());
        return object;
    }
}
