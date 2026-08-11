package com.implantiq;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ApiService {
    private static ApiService instance;
    private RequestQueue requestQueue;
    private static Context ctx;
    private String authToken;
    private String baseUrl = NetworkConfig.BASE_URL;

    private ApiService(Context context) {
        ctx = context.getApplicationContext();
        requestQueue = getRequestQueue();
        SharedPreferences prefs = ctx.getSharedPreferences("ImplantIQ", Context.MODE_PRIVATE);
        authToken = prefs.getString("auth_token", null);
    }

    public static synchronized ApiService getInstance(Context context) {
        if (instance == null) {
            instance = new ApiService(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx);
        }
        return requestQueue;
    }

    public void setAuthToken(String token) {
        this.authToken = token;
        ctx.getSharedPreferences("ImplantIQ", Context.MODE_PRIVATE).edit().putString("auth_token", token).apply();
    }

    public void clearSession() {
        this.authToken = null;
        ctx.getSharedPreferences("ImplantIQ", Context.MODE_PRIVATE).edit().remove("auth_token").apply();
    }

    public String getAuthToken() {
        return authToken;
    }

    public interface ApiCallback<T> {
        void onSuccess(T response);
        void onError(String message);
    }

    public void get(String endpoint, ApiCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, baseUrl + endpoint, null,
                callback::onSuccess,
                error -> callback.onError(handleError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (authToken != null) headers.put("Authorization", authToken);
                return headers;
            }
        };
        addToRequestQueue(request);
    }

    public void getArray(String endpoint, ApiCallback<JSONArray> callback) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, baseUrl + endpoint, null,
                callback::onSuccess,
                error -> callback.onError(handleError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (authToken != null) headers.put("Authorization", authToken);
                return headers;
            }
        };
        addToRequestQueue(request);
    }

    public void post(String endpoint, JSONObject body, ApiCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, baseUrl + endpoint, body,
                callback::onSuccess,
                error -> callback.onError(handleError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (authToken != null) headers.put("Authorization", authToken);
                return headers;
            }
        };
        addToRequestQueue(request);
    }

    private <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1.0f));
        getRequestQueue().add(req);
    }

    private String handleError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
            return "Session Expired. Please login again.";
        }
        return "Network Error: Check clinical server connection.";
    }
}
