package com.cityfix.models;

import com.google.firebase.Timestamp;

/**
 * Domain model representing an application user.
 * Holds profile information (display name, email, avatar colour), the user's
 * assigned role ({@code citizen} or {@code admin}), and the total number of
 * reports submitted. The no-argument constructor is required by the Firestore SDK
 * for automatic object deserialization.
 */
public class User {

    private String userId;
    private String displayName;
    private String email;
    private String role; // "citizen" or "admin"
    private int reportsSubmitted;
    private Timestamp joinedAt;
    private String avatarColor;

    public User() {} // Required for Firestore deserialization

    public User(String userId, String displayName, String email, String role) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.reportsSubmitted = 0;
        this.joinedAt = Timestamp.now();
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    // Getters
    public String getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public int getReportsSubmitted() { return reportsSubmitted; }
    public Timestamp getJoinedAt() { return joinedAt; }
    public String getAvatarColor() { return avatarColor; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setReportsSubmitted(int reportsSubmitted) { this.reportsSubmitted = reportsSubmitted; }
    public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }
}
