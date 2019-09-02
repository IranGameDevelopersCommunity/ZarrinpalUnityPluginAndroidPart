package com.zarinpal.ewallets.purchase;

import org.json.JSONObject;

public interface HttpRequestListener {
    void onFailureResponse(int i, String str);

    void onSuccessResponse(JSONObject jSONObject, String str);
}
