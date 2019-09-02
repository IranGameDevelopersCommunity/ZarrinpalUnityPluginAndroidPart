package com.zarinpal.ewallets.purchase;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentRequest {
    protected static final String PAYMENT_GATEWAY_URL = "https://www.%szarinpal.com/pg/StartPay/%s/ZarinGate";
    protected static final String PAYMENT_REQUEST_URL = "https://www.%szarinpal.com/pg/rest/WebGate/PaymentRequest.json";
    protected static final String VERIFICATION_PAYMENT_URL = "https://www.%szarinpal.com/pg/rest/WebGate/PaymentVerification.json";
    private long amount;
    private String authority;
    private String callBackURL;
    private String description;
    private String email;
    private String merchantID;
    private String mobile;

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public void setCallbackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCallBackURL() {
        return this.callBackURL;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public long getAmount() {
        return this.amount;
    }

    public String getDescription() {
        return this.description;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getMerchantID() {
        return this.merchantID;
    }

    public String getEmail() {
        return this.email;
    }

    public String getAuthority() {
        return this.authority;
    }

    public JSONObject getPaymentRequestAsJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Payment.MERCHANT_ID_PARAMS, getMerchantID());
        jsonObject.put(Payment.AMOUNT_PARAMS, getAmount());
        jsonObject.put(Payment.DESCRIPTION_PARAMS, getDescription());
        jsonObject.put(Payment.CALLBACK_URL_PARAMS, getCallBackURL());
        jsonObject.put(Payment.MOBILE_PARAMS, getMobile());
        jsonObject.put(Payment.EMAIL_PARAMS, getEmail());
        return jsonObject;
    }

    public String getStartPaymentGatewayURL(String authority) {
        return String.format(PAYMENT_GATEWAY_URL, new Object[]{BuildConfig.FLAVOR, authority});
    }

    public String getPaymentRequestURL() {
        return String.format(PAYMENT_REQUEST_URL, new Object[]{BuildConfig.FLAVOR});
    }

    public String getVerificationPaymentURL() {
        return String.format(VERIFICATION_PAYMENT_URL, new Object[]{BuildConfig.FLAVOR});
    }
}
