package com.zarinpal.ewallets.purchase;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

class HttpQueue {
    private static HttpQueue instance;
    private static RequestQueue queue;

    HttpQueue() {
    }

    public static HttpQueue getInstance(Context context) {
        if (instance == null) {
            instance = new HttpQueue();
            queue = Volley.newRequestQueue(context);
        }
        return instance;
    }

    public void addToRequest(Request request) {
        queue.add(request);
    }
}
