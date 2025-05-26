package com.example.univmate;

public class Request {
    private String requestId;
    private String userId;
    private String category;
    private String urgencyLevel;
    private String description;
    private String location;
    private String status;
    private long timestamp;

    // Empty constructor needed for Firestore
    public Request() {}

    // Full constructor
    public Request(String userId, String category, String urgencyLevel,
                   String description, String location, String status) {
        this.userId = userId;
        this.category = category;
        this.urgencyLevel = urgencyLevel;
        this.description = description;
        this.location = location;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getUserId() { return userId; }
    public String getCategory() { return category; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }

    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}