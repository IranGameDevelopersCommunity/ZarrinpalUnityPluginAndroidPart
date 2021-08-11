package com.kingcodestudio.unityzarinpaliab;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.unity3d.player.UnityPlayer;
import com.zarinpal.ewallets.purchase.OnCallbackRequestPaymentListener;
import com.zarinpal.ewallets.purchase.OnCallbackVerificationPaymentListener;
import com.zarinpal.ewallets.purchase.PaymentRequest;
import com.zarinpal.ewallets.purchase.ZarinPal;

public class ZarinpalActivity extends Activity {

    // Unity context.
    private static final String UNITY_GAME_OBJECT_NAME = "ZarinpalAndroid";

    private static String m_merchantID;
    private static String m_callbackScheme;
    public static boolean m_verifyPurchase;
    private static boolean m_autoStartPurchase;

    //Purchase parameters
    private static long m_amount;
    private static String m_description;
    private static String m_title;

    ProgressDialog progress ;
    private static Intent m_purchaseIntent;
    private static Uri m_purchase_data;

    private void showProgress(){
        Resources res = getResources();
        String title = res.getString(R.string.progressTitle);
        String desc = res.getString(R.string.progressDesc);
        progress = new ProgressDialog(this);
        progress.setTitle(title);
        progress.setMessage(desc);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
    }

    private void dismissProgress(){
        if(progress!=null){
            progress.dismiss();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        int actionType =  event.getAction();
        Log.d("Zarinpal","onTouchEvent , actionType : "+actionType);
        if (MotionEvent.ACTION_OUTSIDE == actionType) {
            cancelPurchase();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    private void ResizeActivity(){
        WindowManager.LayoutParams params = getWindow().getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        params.x = -20;
        params.y = -10;
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = 3*height/4;
            params.width = 2 * width/3;
        } else {
            params.height = height/2;
            params.width = 3 * width/4;
        }

        this.getWindow().setAttributes(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        //Returning from payment gateway
        String action = getIntent().getAction();
        Log.d("Zarinpal","Action : "+action);
        if(action!=null && action.equals("android.intent.action.VIEW"))
        {
            setContentView(R.layout.activity_verifying);

            Uri data = getIntent().getData();
            m_purchase_data = data;
            if(data==null)
            {
                Log.d("Zarinpal","zarinpal purchase returned null");
                setupReturnView(false);
            }
            else
            {
                Log.d("Zarinpal","zarinpal purchase returned : "+data.toString());
                String status = data.getQueryParameter("Status");
                if(status!=null && status.equals("OK"))
                {
                    Log.d("Zarinpal","Status OK trying to verify purchase...");
                    String authority = data.getQueryParameter("Authority");
                    UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPurchaseSucceed",authority);
                    if(m_verifyPurchase){
                        Log.d("Zarinpal","Starting to verify purchase cause autoVerify is set to true : ");
                        verifyPurchase();
                        finish();
                    }
                    else
                    {
                        Log.d("Zarinpal","Ignore verifying purchase cause autoVerify is set to false : ");
                        setupReturnView(true);
                        finish();//Finish purchase flow and return o unity
                    }
                }
                else
                {
                    Log.d("Zarinpal","purchase failed : "+status);
                    UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPurchaseFailed","null");
                    setupReturnView(false);
                    this.finish();
                }
            }
        }
        //finish activity because we have no data to handle
        else
        {
            setContentView(R.layout.activity_zarinpal);
            TextView titleTextView = findViewById(R.id.textViewTitle);
            titleTextView.setText(m_title);
            Resources res = getResources();
            String amountCurrencyString = String.format("%,d", m_amount);
            String priceText = res.getString(R.string.priceText, amountCurrencyString);
            TextView priceTextView = findViewById(R.id.priceTextview);
            priceTextView.setText(priceText);
            final Button purchaseButton = findViewById(R.id.purchaseButton);
            final Button cancelButton = findViewById(R.id.cancelButton);
            purchaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelButton.setEnabled(false);
                    purchaseButton.setEnabled(false);
                    purchase(m_amount,m_description);
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelPurchase();
                }
            });
        }

        //ResizeActivity();
    }

    public static void initialize(String merchantID,boolean verifyPurchase,String callbackScheme,boolean autoStartPurchase){
        m_merchantID = merchantID; // Store merchant id
        m_verifyPurchase = verifyPurchase;//Should verify purchase after purchase completed
        m_callbackScheme = callbackScheme;//App Scheme used for zarinpal callback
        m_autoStartPurchase = autoStartPurchase;
        UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnStoreInitialized","store initialized");
    }

    private void cancelPurchase(){
        Log.d("Zarinpal","Purchase canceled by user");
        UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPurchaseCanceled","cancel by user");
        finish();
    }


    public static void startPurchaseFlow(long amount,String title,String desc)
    {
        m_amount = amount;
        m_description = desc;
        m_title = title;
        Intent intent = new Intent(UnityPlayer.currentActivity,ZarinpalActivity.class);
        UnityPlayer.currentActivity.startActivity(intent);
    }

    public static void startPurchaseFlowWithoutPanel(long amount, String description, String emailText, String mobileText){
        m_purchaseIntent = null;
        ZarinPal purchase = ZarinPal.getPurchase(UnityPlayer.currentActivity);
        PaymentRequest payment = ZarinPal.getPaymentRequest();
        payment.setMerchantID(m_merchantID);
        payment.setAmount(amount);
        payment.setDescription(description);
        payment.setCallbackURL(m_callbackScheme);
        if(!TextUtils.isEmpty(emailText)){
            payment.setEmail(emailText);
        }
        if(!TextUtils.isEmpty(mobileText)){
            payment.setMobile(mobileText);
        }
        Log.d("Zarinpal","Creating purchase object with callback : "+m_callbackScheme);

        purchase.startPayment(payment, new OnCallbackRequestPaymentListener() {
            @Override
            public void onCallbackResultPaymentRequest(int status, String authority, Uri paymentGatewayUri, Intent intent, String error) {
                if(status==100){
                    Log.d("Zarinpal","Payment started");
                    UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPurchaseStarted",authority);
                    if(m_autoStartPurchase){
                        UnityPlayer.currentActivity.startActivity(intent);
                    }
                    m_purchaseIntent = intent;
                }
                else
                {
                    Log.d("Zarinpal","Payment failed to start");
                    UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPurchaseFailedToStart",error);
                }
            }
        });
    }

    private void purchase(long amount, String description){
        m_purchaseIntent = null;
        EditText emailText = findViewById(R.id.emailText);
        EditText mobileText = findViewById(R.id.mobileText);
        ZarinPal purchase = ZarinPal.getPurchase(UnityPlayer.currentActivity);
        PaymentRequest payment = ZarinPal.getPaymentRequest();
        payment.setMerchantID(m_merchantID);
        payment.setAmount(amount);
        payment.setDescription(description);
        payment.setCallbackURL(m_callbackScheme);
        if(!TextUtils.isEmpty(emailText.getText().toString())){
            payment.setEmail(emailText.getText().toString());
        }
        if(!TextUtils.isEmpty(mobileText.getText().toString())){
            payment.setMobile(mobileText.getText().toString());
        }
        Log.d("Zarinpal","Creating purchase object with callback : "+m_callbackScheme);

        showProgress();
        purchase.startPayment(payment, new OnCallbackRequestPaymentListener() {
            @Override
            public void onCallbackResultPaymentRequest(int status, String authority, Uri paymentGatewayUri, Intent intent, String error) {
                if(status==100){
                    Log.d("Zarinpal","Payment started");
                    UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPurchaseStarted",authority);
                    if(m_autoStartPurchase){
                        UnityPlayer.currentActivity.startActivity(intent);
                    }
                    m_purchaseIntent = intent;
                }
                else
                {
                    Log.d("Zarinpal","Payment failed to start");
                    UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPurchaseFailedToStart",error);
                }
                dismissProgress();
                finish();

            }
        });
    }

    public static void startPurchaseActivity(){
        if(m_purchaseIntent!=null){
            UnityPlayer.currentActivity.startActivity(m_purchaseIntent);
        }
        else
        {
            UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPurchaseFailedToStart","m_purchaseIntent is null.make sure to call startPurchaseFlow first");
        }
    }

    public static void verifyPurchase(String authority,int amount){
        m_purchase_data = Uri.parse("dummyscheme://dummyhost?Authority="+authority+"&Status=OK");
        PaymentRequest payment = ZarinPal.getPaymentRequest();
        payment.setMerchantID(m_merchantID);
        payment.setAuthority(authority);
        payment.setAmount(amount);
        ZarinPal verify = ZarinPal.getPurchase(UnityPlayer.currentActivity);
        verify.setPayment(payment);
        Log.d("Zarinpal","purchase_data created : "+m_purchase_data.toString());
        verifyPurchase();
    }

    public static void verifyPurchase(){
        if(m_purchase_data!=null)
        {
            Log.d("Zarinpal","Request for verify purchase : "+m_purchase_data.toString());
            UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPaymentVerificationStarted",m_purchase_data.toString());
            String status = m_purchase_data.getQueryParameter("Status");
            if(status!=null && status.equals("OK")){
                Log.d("Zarinpal","Status ok . try to verify : "+m_purchase_data.toString());

                ZarinPal.getPurchase(UnityPlayer.currentActivity).verificationPayment(m_purchase_data, new OnCallbackVerificationPaymentListener() {
                    @Override
                    public void onCallbackResultVerificationPayment(boolean isPaymentSuccess, String refID, PaymentRequest paymentRequest)
                    {

                        Log.d("Zarinpal","verification completed : "+m_purchase_data.toString());
                        if(isPaymentSuccess)
                        {
                            Log.d("Zarinpal","Payment verify success");
                            UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPaymentVerificationSucceed",refID);
                        }
                        else {
                            Log.d("Zarinpal","Payment verify failed");
                            UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPaymentVerificationFailed","purchase is not valid");

                        }
                    }
                });
            }
            else
            {
                Log.d("Zarinpal","status not ok . verify failed : "+m_purchase_data.toString());
                UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPaymentVerificationFailed","purchase status is NOK");
            }
        }
        else
        {
            Log.d("Zarinpal","unable to verify purchase because data is null ");
        }

    }

    private void setupReturnView(boolean success){
        TextView resultTextView = findViewById(R.id.purchase_result_textView);
        Resources res = getResources();
        final Button returnButton = findViewById(R.id.returnButton);

        if(success){
            String successText = res.getString(R.string.purchase_success);
            resultTextView.setText(successText);
        }
        else
        {
            String failedText = res.getString(R.string.purchase_failed);
            resultTextView.setText(failedText);
        }

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
