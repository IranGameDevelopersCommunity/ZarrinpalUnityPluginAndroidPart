package com.zarinpal.ewallets.purchase;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequest implements Listener, ErrorListener {
    private static final String DEFAULT_ERROR = "Http error incorrect.";
    public static final int DELETE = 3;
    public static final byte FROM_DATA = (byte) 1;
    public static final int GET = 0;
    public static final int INTERNET_CONNECTION_ERROR = -100;
    public static final int POST = 1;
    public static final int PUT = 2;
    public static final byte RAW = (byte) 0;
    private static final int TIMEOUT_DEFAULT_VALUE = 10000;
    public static final int TIMEOUT_ERROR = -101;
    public static final int UNKNOWN_ERROR = -102;
    private Context context;
    private Map<String, String> headers = new HashMap();
    private JSONObject jsonObject;
    private HttpRequestListener listener;
    private Map<String, String> params = new HashMap();
    private Request request;
    private int requestMethod;
    private byte requestType;
    private int timeOut;
    private String url;

    public HttpRequest(Context context, String url) {
        this.url = url;
        this.context = context;
    }

    public HttpRequest setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public HttpRequest setJson(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }

    public HttpRequest setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequest setRequestType(byte RequestType) {
        this.requestType = RequestType;
        return this;
    }

    public HttpRequest setRequestMethod(int method) {
        this.requestMethod = method;
        return this;
    }

    public HttpRequest setTimeOut(int timeOut) {
        this.timeOut = timeOut * 1000;
        return this;
    }

    public void onResponse(Object response) {
        if (isJsonValid(response)) {
            try {
                this.listener.onSuccessResponse(new JSONObject(response.toString()), response.toString());
                return;
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
        this.listener.onSuccessResponse(null, response.toString());
    }

    public void onErrorResponse(VolleyError error) {
        if (error instanceof NoConnectionError) {
            this.listener.onFailureResponse(-100, DEFAULT_ERROR);
        } else if (error instanceof TimeoutError) {
            this.listener.onFailureResponse(TIMEOUT_ERROR, DEFAULT_ERROR);
        } else if (error.networkResponse == null) {
            this.listener.onFailureResponse(UNKNOWN_ERROR, DEFAULT_ERROR);
        } else {
            this.listener.onFailureResponse(error.networkResponse.statusCode, new String(error.networkResponse.data));
            Log.i("TAG Error HttpRequest", new String(error.networkResponse.data));
        }
    }

    public void get(HttpRequestListener listener) {
        this.listener = listener;
        if (this.requestType == (byte) 1) {
            this.request = new StringRequest(this.requestMethod, this.url, this, this) {
                protected Map<String, String> getParams() throws AuthFailureError {
                    return HttpRequest.this.params;
                }

                public Map<String, String> getHeaders() throws AuthFailureError {
                    return HttpRequest.this.headers;
                }
            };
        } else {
            this.request = new JsonObjectRequest(this.requestMethod, this.url, this.jsonObject == null ? new JSONObject(this.params) : this.jsonObject, this, this) {
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return HttpRequest.this.headers;
                }
            };
        }
        this.request.setRetryPolicy(new DefaultRetryPolicy(this.timeOut == 0 ? TIMEOUT_DEFAULT_VALUE : this.timeOut, 0, 1.0f));
        HttpQueue.getInstance(this.context).addToRequest(this.request);
    }

    private boolean isJsonValid(Object object) {
        try {
            JSONObject jSONObject = new JSONObject(object.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
