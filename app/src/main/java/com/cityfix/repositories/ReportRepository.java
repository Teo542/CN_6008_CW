package com.cityfix.repositories;

import androidx.lifecycle.MutableLiveData;

import com.cityfix.models.FaultReport;
import com.cityfix.utils.Constants;
import com.cityfix.models.Comment;
import com.cityfix.models.StatusUpdate;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for fault report operations against Cloud Firestore.
 * Encapsulates all CRUD, upvote, comment, and status-history operations for
 * the {@code reports} collection. Real-time listeners push updates directly
 * into {@link androidx.lifecycle.MutableLiveData} instances so the UI stays
 * in sync without manual polling. Call {@link #removeListener()} when the
 * owning component is destroyed to avoid memory leaks.
 */
public class ReportRepository {

    private final FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private final java.util.List<ListenerRegistration> allListeners = new java.util.ArrayList<>();

    public ReportRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<DocumentReference> addReport(FaultReport report) {
        return db.collection(Constants.COLLECTION_REPORTS).add(report);
    }

    public Task<Void> updateStatus(String reportId, String newStatus) {
        return db.collection(Constants.COLLECTION_REPORTS)
                .document(reportId)
                .update("status", newStatus);
    }

    public Task<QuerySnapshot> getAllReports() {
        return db.collection(Constants.COLLECTION_REPORTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    // Real-time listener — pushes updates to LiveData automatically
    public void listenToReports(MutableLiveData<List<FaultReport>> liveData) {
        listenerRegistration = db.collection(Constants.COLLECTION_REPORTS)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<FaultReport> reports = new ArrayList<>();
                    for (var doc : snapshots.getDocuments()) {
                        FaultReport report = doc.toObject(FaultReport.class);
                        if (report != null) {
                            report.setReportId(doc.getId());
                            reports.add(report);
                        }
                    }
                    liveData.postValue(reports);
                });
    }

    public Task<DocumentReference> addComment(String reportId, Comment comment) {
        return db.collection(Constants.COLLECTION_REPORTS)
                .document(reportId)
                .collection(Constants.COLLECTION_COMMENTS)
                .add(comment);
    }

    public void listenToComments(String reportId, MutableLiveData<List<Comment>> liveData) {
        ListenerRegistration reg = db.collection(Constants.COLLECTION_REPORTS)
                .document(reportId)
                .collection(Constants.COLLECTION_COMMENTS)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Comment> list = new ArrayList<>();
                    for (var doc : snapshots.getDocuments()) {
                        Comment c = doc.toObject(Comment.class);
                        if (c != null) {
                            c.setCommentId(doc.getId());
                            list.add(c);
                        }
                    }
                    liveData.postValue(list);
                });
        allListeners.add(reg);
    }

    public Task<DocumentReference> addStatusUpdate(String reportId, StatusUpdate update) {
        return db.collection(Constants.COLLECTION_REPORTS)
                .document(reportId)
                .collection(Constants.COLLECTION_STATUS_HISTORY)
                .add(update);
    }

    public Task<QuerySnapshot> getStatusHistory(String reportId) {
        return db.collection(Constants.COLLECTION_REPORTS)
                .document(reportId)
                .collection(Constants.COLLECTION_STATUS_HISTORY)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get();
    }

    public Task<Void> upvoteReport(String reportId, String userId) {
        var ref = db.collection(Constants.COLLECTION_REPORTS).document(reportId);
        return db.runTransaction(transaction -> {
            var snap = transaction.get(ref);
            java.util.List<?> upvoterIds = (java.util.List<?>) snap.get("upvoterIds");
            if (upvoterIds != null && upvoterIds.contains(userId)) return null;
            transaction.update(ref, "upvotes", FieldValue.increment(1));
            transaction.update(ref, "upvoterIds", FieldValue.arrayUnion(userId));
            return null;
        });
    }

    public Task<DocumentSnapshot> hasUpvoted(String reportId, String userId) {
        return db.collection(Constants.COLLECTION_REPORTS).document(reportId).get();
    }

    public Task<DocumentSnapshot> getReport(String reportId) {
        return db.collection(Constants.COLLECTION_REPORTS).document(reportId).get();
    }

    public Task<QuerySnapshot> getUserReports(String userId) {
        return db.collection(Constants.COLLECTION_REPORTS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    public void listenToUserReports(String userId, MutableLiveData<List<FaultReport>> liveData) {
        ListenerRegistration reg = db.collection(Constants.COLLECTION_REPORTS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<FaultReport> reports = new ArrayList<>();
                    for (var doc : snapshots.getDocuments()) {
                        FaultReport report = doc.toObject(FaultReport.class);
                        if (report != null) {
                            report.setReportId(doc.getId());
                            reports.add(report);
                        }
                    }
                    liveData.postValue(reports);
                });
        allListeners.add(reg);
    }

    public void removeListener() {
        if (listenerRegistration != null) listenerRegistration.remove();
        for (ListenerRegistration r : allListeners) r.remove();
        allListeners.clear();
    }
}
