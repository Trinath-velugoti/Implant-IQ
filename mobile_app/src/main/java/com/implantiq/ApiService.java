package com.implantiq;

import android.content.Context;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

public class ApiService {
    private static final String TAG = "ApiService";
    private static ApiService instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private String baseUrl = NetworkConfig.BASE_URL;

    private ApiService(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized ApiService getInstance(Context context) {
        if (instance == null) {
            instance = new ApiService(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public interface ApiCallback<T> {
        void onSuccess(T response);
        void onError(String message);
    }

    public void get(String endpoint, ApiCallback<JSONObject> callback) {
        String url = baseUrl + endpoint;
        Log.d(TAG, "Executing GET: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                callback::onSuccess,
                error -> callback.onError(handleError(error))
        );
        addToRequestQueue(request);
    }

    public void getArray(String endpoint, ApiCallback<JSONArray> callback) {
        String url = baseUrl + endpoint;
        Log.d(TAG, "Executing GET Array: " + url);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                callback::onSuccess,
                error -> callback.onError(handleError(error))
        );
        addToRequestQueue(request);
    }

    public void post(String endpoint, JSONObject body, ApiCallback<JSONObject> callback) {
        String url = baseUrl + endpoint;
        Log.d(TAG, "Executing POST: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                callback::onSuccess,
                error -> callback.onError(handleError(error))
        );
        addToRequestQueue(request);
    }

    private <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1.0f));
        getRequestQueue().add(req);
    }

    private String handleError(com.android.volley.VolleyError error) {
        Log.e(TAG, "Network Error on URL: " + baseUrl);
        if (error instanceof com.android.volley.NoConnectionError || error instanceof com.android.volley.NetworkError) {
            return "Connection Failed. Check if phone & laptop are on SAME Wi-Fi.";
        } else if (error instanceof com.android.volley.TimeoutError) {
            return "Request Timed Out. Backend is unreachable.";
        } else if (error.networkResponse != null) {
            return "Server error: " + error.networkResponse.statusCode;
        }
        return "Unknown Connection Error: " + error.toString();
    }
}
