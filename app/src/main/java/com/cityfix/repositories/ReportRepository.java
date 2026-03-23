package com.cityfix.repositories;

import androidx.lifecycle.MutableLiveData;

import com.cityfix.models.FaultReport;
import com.cityfix.utils.Constants;
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

public class ReportRepository {

    private final FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

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

    public Task<Void> upvoteReport(String reportId) {
        return db.collection(Constants.COLLECTION_REPORTS)
                .document(reportId)
                .update("upvotes", FieldValue.increment(1));
    }

    public Task<DocumentSnapshot> getReport(String reportId) {
        return db.collection(Constants.COLLECTION_REPORTS).document(reportId).get();
    }

    public void listenToUserReports(String userId, MutableLiveData<List<FaultReport>> liveData) {
        db.collection(Constants.COLLECTION_REPORTS)
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
    }

    public void removeListener() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
