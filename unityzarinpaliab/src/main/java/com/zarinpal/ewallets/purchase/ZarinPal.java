package com.zarinpal.ewallets.purchase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint({"StaticFieldLeak"})
public class ZarinPal {
    private static ZarinPal instance;
    private Context context;
    private PaymentRequest paymentRequest;

    public static ZarinPal getPurchase(Context context) {
        if (instance == null) {
            instance = new ZarinPal(context);
        }
        return instance;
    }

    public static PaymentRequest getPaymentRequest() {
        return new PaymentRequest();
    }

    public static SandboxPaymentRequest getSandboxPaymentRequest() {
        return new SandboxPaymentRequest();
    }

    private ZarinPal(Context context) {
        this.context = context;
    }

    public void setPayment(PaymentRequest payment) {
        paymentRequest = payment;
    }

    public void verificationPayment(Uri uri, final OnCallbackVerificationPaymentListener listener) {
        if (uri == null || this.paymentRequest == null || !uri.isHierarchical()) {
            Log.d("Zarinpal","can not verify purchase , because paymentrequest is null");
            return;
        }
        boolean isSuccess = uri.getQueryParameter("Status").equals("OK");
        String authority = uri.getQueryParameter("Authority");
        if (!authority.equals(this.paymentRequest.getAuthority())) {
            listener.onCallbackResultVerificationPayment(false, null, this.paymentRequest);
        } else if (isSuccess) {
            VerificationPayment verificationPayment = new VerificationPayment();
            verificationPayment.setAmount(this.paymentRequest.getAmount());
            verificationPayment.setMerchantID(this.paymentRequest.getMerchantID());
            verificationPayment.setAuthority(authority);
            try {
                new HttpRequest(this.context, this.paymentRequest.getVerificationPaymentURL()).setJson(verificationPayment.getVerificationPaymentAsJson()).setRequestMethod(1).setRequestType((byte) 0).get(new HttpRequestListener() {
                    public void onSuccessResponse(JSONObject jsonObject, String contentResponse) {
                        try {
                            JSONObject error = jsonObject.optJSONObject("errors");
                            if(error!=null){
                                listener.onCallbackResultVerificationPayment(false, null, ZarinPal.this.paymentRequest);
                            }
                            else{
                                JSONObject data = jsonObject.getJSONObject("data");
                                listener.onCallbackResultVerificationPayment(true, data.getString("ref_id"), ZarinPal.this.paymentRequest);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    public void onFailureResponse(int httpStatusCode, String dataError) {
                        listener.onCallbackResultVerificationPayment(false, null, ZarinPal.this.paymentRequest);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            listener.onCallbackResultVerificationPayment(false, null, this.paymentRequest);
        }
    }

    public void startPayment(final PaymentRequest paymentRequest, final OnCallbackRequestPaymentListener listener) {
        this.paymentRequest = paymentRequest;
        try {
            Log.d("Zarinpal", "startPayment: here");
            new HttpRequest(this.context, paymentRequest.getPaymentRequestURL()).setRequestType((byte) 0).setRequestMethod(1).setJson(paymentRequest.getPaymentRequestAsJson()).get(new HttpRequestListener() {
                public void onSuccessResponse(JSONObject jsonObject, String contentResponse) {
                    try {
                        Log.d("Zarinpal", "jsonContent:"+jsonObject.toString());
                        JSONObject error = jsonObject.optJSONObject("errors");
                        if(error!=null){
                            listener.onCallbackResultPaymentRequest(error.getInt("code"), null, null, null, error.getString("message"));
                            return;
                        }
                        else
                        {
                            JSONObject data = jsonObject.getJSONObject("data");
                            int status = data.getInt("code");
                            String authority = data.getString(Payment.AUTHORITY_PARAMS);
                            paymentRequest.setAuthority(authority);
                            Uri uri = Uri.parse(paymentRequest.getStartPaymentGatewayURL(authority));
                            listener.onCallbackResultPaymentRequest(status, authority, uri, new Intent("android.intent.action.VIEW", uri), null);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onCallbackResultPaymentRequest(400, null, null, null, "response is not json");
                    }
                }

                public void onFailureResponse(int httpStatusCode, String dataError) {
                    try {
                        JSONObject jsonObject = new JSONObject(dataError);
                        JSONObject error = jsonObject.optJSONObject("errors");
                        if(error!=null){
                            listener.onCallbackResultPaymentRequest(error.getInt("code"), null, null, null, error.getString("message"));
                        }
                        else{
                            listener.onCallbackResultPaymentRequest(httpStatusCode, null, null, null, "http failed, " + dataError);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onCallbackResultPaymentRequest(httpStatusCode, null, null, null, "failed an http request");
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
