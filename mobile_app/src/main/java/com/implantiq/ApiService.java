package com.implantiq;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
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

    public String getAuthToken() { return authToken; }

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

    public void uploadImage(String endpoint, Bitmap bitmap, String patientId, ApiCallback<JSONObject> callback) {
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, baseUrl + endpoint,
                response -> {
                    try {
                        String resultResponse = new String(response.data);
                        callback.onSuccess(new JSONObject(resultResponse));
                    } catch (Exception e) { callback.onError("Response Parse Error"); }
                },
                error -> callback.onError(handleError(error))
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patientId", patientId);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("file", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (authToken != null) headers.put("Authorization", authToken);
                return headers;
            }
        };
        addToRequestQueue(multipartRequest);
    }

    private byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(30000, 1, 1.0f));
        getRequestQueue().add(req);
    }

    private String handleError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null) {
            if(error.networkResponse.statusCode == 401) return "Session Expired";
            if(error.networkResponse.statusCode == 422) return "X-Ray Validation Failed";
        }
        return "Network Error: Please check connection";
    }

    // Custom class for Multipart
    class VolleyMultipartRequest extends Request<NetworkResponse> {
        private final Response.Listener<NetworkResponse> mListener;
        private final Response.ErrorListener mErrorListener;

        public VolleyMultipartRequest(int method, String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.mListener = listener;
            this.mErrorListener = errorListener;
        }

        @Override
        protected Map<String, String> getParams() { return null; }

        @Override
        protected void deliverResponse(NetworkResponse response) { mListener.onResponse(response); }

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
            try { return Response.success(response, HttpHeaderParser.parseCacheHeaders(response)); } 
            catch (Exception e) { return Response.error(new com.android.volley.ParseError(e)); }
        }

        protected Map<String, DataPart> getByteData() { return null; }

        @Override
        public String getBodyContentType() { return "multipart/form-data;boundary=" + boundary; }

        private final String boundary = "apiclient-" + System.currentTimeMillis();

        @Override
        public byte[] getBody() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                // write text params
                Map<String, String> params = getParams();
                if (params != null && params.size() > 0) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        buildTextPart(bos, entry.getKey(), entry.getValue());
                    }
                }
                // write byte data
                Map<String, DataPart> data = getByteData();
                if (data != null && data.size() > 0) {
                    for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                        buildDataPart(bos, entry.getValue(), entry.getKey());
                    }
                }
                bos.write(("--" + boundary + "--\r\n").getBytes());
            } catch (Exception e) { Log.e("ApiService", "IOException writing to ByteArrayOutputStream"); }
            return bos.toByteArray();
        }

        private void buildTextPart(ByteArrayOutputStream bos, String parameterName, String parameterValue) throws Exception {
            bos.write(("--" + boundary + "\r\n").getBytes());
            bos.write(("Content-Disposition: form-data; name=\"" + parameterName + "\"\r\n\r\n").getBytes());
            bos.write((parameterValue + "\r\n").getBytes());
        }

        private void buildDataPart(ByteArrayOutputStream bos, DataPart dataFile, String inputName) throws Exception {
            bos.write(("--" + boundary + "\r\n").getBytes());
            bos.write(("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + dataFile.getFileName() + "\"\r\n").getBytes());
            if (dataFile.getType() != null && !dataFile.getType().isEmpty()) {
                bos.write(("Content-Type: " + dataFile.getType() + "\r\n").getBytes());
            }
            bos.write("\r\n".getBytes());
            bos.write(dataFile.getContent());
            bos.write("\r\n".getBytes());
        }
    }

    class DataPart {
        private String fileName;
        private byte[] content;
        private String type;
        public DataPart(String name, byte[] data) { this.fileName = name; this.content = data; }
        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }
}
