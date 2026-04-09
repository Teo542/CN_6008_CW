package com.cityfix.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Reserved for future push-notification support.
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Reserved for future FCM token persistence.
    }
}
