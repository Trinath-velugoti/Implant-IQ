package com.implantiq;

public class NetworkConfig {
    /**
     * CENTRAL NETWORK CONFIGURATION
     * Change the IP address here once, and it updates the entire app.
     */
    public static final String IP_ADDRESS = "10.68.228.120"; // FINAL VERIFIED IP
    public static final String BASE_URL = "http://" + IP_ADDRESS + ":8080";
    
    // API Endpoints
    public static final String API_STATS = "/api/stats";
    public static final String API_RECENT = "/api/recent";
    public static final String API_PATIENTS = "/api/patients";
    public static final String API_PREDICT = "/api/predict";
    public static final String API_HEALTH = "/api/health";
}
