package com.zarinpal.ewallets.purchase;

public class SandboxPaymentRequest extends PaymentRequest {
    private static final String SANDBOX = "sandbox.";
    private static final String WORLD_WIDE_WEB = "www.";

    public String getPaymentRequestURL() {
        return String.format("https://www.%szarinpal.com/pg/rest/WebGate/PaymentRequest.json", new Object[]{SANDBOX}).replace(WORLD_WIDE_WEB, BuildConfig.FLAVOR);
    }

    public String getStartPaymentGatewayURL(String authority) {
        return String.format("https://www.%szarinpal.com/pg/StartPay/%s/ZarinGate", new Object[]{SANDBOX, authority}).replace(WORLD_WIDE_WEB, BuildConfig.FLAVOR);
    }

    public String getVerificationPaymentURL() {
        return String.format("https://www.%szarinpal.com/pg/rest/WebGate/PaymentVerification.json", new Object[]{SANDBOX}).replace(WORLD_WIDE_WEB, BuildConfig.FLAVOR);
    }
}
