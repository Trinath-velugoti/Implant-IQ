package com.implantiq;

public class NetworkConfig {
    public static final String IP_ADDRESS = "10.68.228.120"; 
    public static final String BASE_URL = "http://" + IP_ADDRESS + ":8080";
    
    // API Endpoints
    public static final String API_LOGIN = "/api/auth/login";
    public static final String API_SIGNUP = "/api/auth/signup";
    public static final String API_VERIFY_OTP = "/api/auth/verify-otp";
    
    public static final String API_DASHBOARD_STATS = "/api/dashboard/statistics";
    public static final String API_PATIENTS = "/api/patients";
    
    public static final String API_XRAY_UPLOAD = "/api/xray/upload";
    public static final String API_XRAY_VALIDATE = "/api/xray/validate";
    public static final String API_XRAY_EXTRACT = "/api/xray/extract";
    public static final String API_PREDICT = "/api/predictions";
}
