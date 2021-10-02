package com.example.taskman;

import com.example.taskman.common.Logs;
import com.example.taskman.common.Tasker;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import lombok.NonNull;

public class MyFirebaseService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //firebase push notification received....
        super.onMessageReceived(remoteMessage);
        Logs.info("FCM received: " + remoteMessage.getData());
        Tasker.sendData(this, remoteMessage.getData().get("title"), remoteMessage.getData().get("data"));
    }
}
