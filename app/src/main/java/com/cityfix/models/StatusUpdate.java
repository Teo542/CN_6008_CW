package com.cityfix.models;

import com.google.firebase.Timestamp;

/**
 * Domain model representing a single audit-trail entry for a report status change.
 * Records the previous and new status values, the admin who made the change,
 * an optional explanatory note, and the timestamp of the transition. The admin
 * portal stores transitions as fromStatus/toStatus, while early Android code
 * used previousStatus/newStatus; both names are accepted for old data.
 * The no-argument constructor is required by the Firestore SDK for automatic
 * object deserialization.
 */
public class StatusUpdate {

    private String updateId;
    private String previousStatus;
    private String newStatus;
    private String fromStatus;
    private String toStatus;
    private String changedBy; // userName of admin
    private String note;
    private Timestamp timestamp;

    public StatusUpdate() {} // Required for Firestore deserialization

    public StatusUpdate(String previousStatus, String newStatus, String changedBy, String note) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.fromStatus = previousStatus;
        this.toStatus = newStatus;
        this.changedBy = changedBy;
        this.note = note;
        this.timestamp = Timestamp.now();
    }

    // Getters
    public String getUpdateId() { return updateId; }
    public String getPreviousStatus() {
        return previousStatus != null ? previousStatus : fromStatus;
    }
    public String getNewStatus() {
        return newStatus != null ? newStatus : toStatus;
    }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getChangedBy() { return changedBy; }
    public String getNote() { return note; }
    public Timestamp getTimestamp() { return timestamp; }

    // Setters
    public void setUpdateId(String updateId) { this.updateId = updateId; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public void setNote(String note) { this.note = note; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
