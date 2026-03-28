package com.cityfix.models;

import com.google.firebase.Timestamp;

/**
 * Domain model representing a comment posted on a fault report.
 * Tracks the author, comment text, creation timestamp, and whether the comment
 * was made by an official (admin) account. The no-argument constructor is
 * required by the Firestore SDK for automatic object deserialization.
 */
public class Comment {

    private String commentId;
    private String text;
    private String userId;
    private String userName;
    private boolean isOfficial;
    private Timestamp timestamp;

    public Comment() {} // Required for Firestore deserialization

    public Comment(String text, String userId, String userName, boolean isOfficial) {
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.isOfficial = isOfficial;
        this.timestamp = Timestamp.now();
    }

    // Getters
    public String getCommentId() { return commentId; }
    public String getText() { return text; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public boolean isOfficial() { return isOfficial; }
    public Timestamp getTimestamp() { return timestamp; }

    // Setters
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public void setText(String text) { this.text = text; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setOfficial(boolean official) { isOfficial = official; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
