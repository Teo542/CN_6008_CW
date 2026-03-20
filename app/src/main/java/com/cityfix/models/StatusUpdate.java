package com.cityfix.models;

import com.google.firebase.Timestamp;

public class StatusUpdate {

    private String updateId;
    private String previousStatus;
    private String newStatus;
    private String changedBy; // userName of admin
    private String note;
    private Timestamp timestamp;

    public StatusUpdate() {} // Required for Firestore deserialization

    public StatusUpdate(String previousStatus, String newStatus, String changedBy, String note) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.note = note;
        this.timestamp = Timestamp.now();
    }

    // Getters
    public String getUpdateId() { return updateId; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getChangedBy() { return changedBy; }
    public String getNote() { return note; }
    public Timestamp getTimestamp() { return timestamp; }

    // Setters
    public void setUpdateId(String updateId) { this.updateId = updateId; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public void setNote(String note) { this.note = note; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
