package com.cityfix.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // TODO Sprint 4: show notification when report status changes
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // TODO Sprint 4: save token to Firestore for push notifications
    }
}
