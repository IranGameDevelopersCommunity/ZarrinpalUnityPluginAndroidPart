package com.zarinpal.ewallets.purchase;

import android.content.Intent;
import android.net.Uri;

public interface OnCallbackRequestPaymentListener {
    void onCallbackResultPaymentRequest(int i, String str, Uri uri, Intent intent);
}
