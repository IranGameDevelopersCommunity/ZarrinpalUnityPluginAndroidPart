package com.kingcodestudio.unityzarinpaliab;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.unity3d.player.UnityPlayer;
import com.zarinpal.ewallets.purchase.OnCallbackVerificationPaymentListener;
import com.zarinpal.ewallets.purchase.PaymentRequest;
import com.zarinpal.ewallets.purchase.ZarinPal;

public class ZarinpalResultActivity extends Activity {

    // Unity context.
    private static final String UNITY_GAME_OBJECT_NAME = "ZarinpalAndroid";

    private boolean m_verifyingCompleted;

    ProgressDialog progress ;

    private void showProgress(){
        Resources res = getResources();
        String title = res.getString(R.string.verifyTitle);
        String desc = res.getString(R.string.verifyDesc);
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
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            cancelPurchase();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Returning from payment gateway
        String action = getIntent().getAction();
        Log.d("Zarinpal","Action : "+action);
        if(action!=null && action.equals("android.intent.action.VIEW"))
        {
            setContentView(R.layout.activity_verifying);

            Uri data = getIntent().getData();
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
                    if(ZarinpalActivity.m_verifyPurchase){
                        Log.d("Zarinpal","Starting to verify purchase cause autoVerify is set to true : ");
                        verifyPurchase(data);
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
        else
        {
            setContentView(R.layout.activity_zarinpal);
        }
    }

    private void verifyPurchase(Uri data){
        Log.d("Zarinpal","Verifying purchase for : "+data.toString());
        UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPaymentVerificationStarted",data.toString());
        //showProgress();
        ZarinPal.getPurchase(UnityPlayer.currentActivity).verificationPayment(data, new OnCallbackVerificationPaymentListener() {
            @Override
            public void onCallbackResultVerificationPayment(boolean isPaymentSuccess, String refID, PaymentRequest paymentRequest)
            {

                if(isPaymentSuccess)
                {
                    Log.d("Zarinpal","Payment verify success");
                    UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPaymentVerificationSucceed",refID);
                    setupReturnView(true);
                }
                else {
                    Log.d("Zarinpal","Payment verify failed");
                    UnityPlayer.UnitySendMessage(UNITY_GAME_OBJECT_NAME,"OnPaymentVerificationFailed","purchase is not valid");
                    setupReturnView(false);

                }
                m_verifyingCompleted = true;
                //dismissProgress();
                finish();
            }
        });
    }

    private void cancelPurchase(){
        if(m_verifyingCompleted)
        {
            finish();
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
