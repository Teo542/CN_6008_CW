package com.cityfix.repositories;

import com.cityfix.models.User;
import com.cityfix.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public Task<Void> createUser(User user) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user);
    }

    public Task<DocumentSnapshot> getUser(String userId) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get();
    }

    public Task<Void> incrementReportsSubmitted(String userId) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("reportsSubmitted",
                        com.google.firebase.firestore.FieldValue.increment(1));
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}
