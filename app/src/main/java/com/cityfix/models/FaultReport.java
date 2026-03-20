package com.cityfix.models;

import com.google.firebase.Timestamp;

public class FaultReport {

    private String reportId;
    private String title;
    private String description;
    private String category; // pothole, streetlight, flooding, vandalism, other
    private String status;   // open, in_progress, resolved
    private double latitude;
    private double longitude;
    private String address;
    private String imageUrl;
    private String userId;
    private String userName;
    private int upvotes;
    private Timestamp timestamp;
    private Timestamp resolvedAt;

    public FaultReport() {} // Required for Firestore deserialization

    public FaultReport(String title, String description, String category,
                       double latitude, double longitude, String address,
                       String userId, String userName) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = "open";
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.userId = userId;
        this.userName = userName;
        this.upvotes = 0;
        this.timestamp = Timestamp.now();
    }

    // Getters
    public String getReportId() { return reportId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAddress() { return address; }
    public String getImageUrl() { return imageUrl; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getUpvotes() { return upvotes; }
    public Timestamp getTimestamp() { return timestamp; }
    public Timestamp getResolvedAt() { return resolvedAt; }

    // Setters
    public void setReportId(String reportId) { this.reportId = reportId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(String status) { this.status = status; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setAddress(String address) { this.address = address; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}
