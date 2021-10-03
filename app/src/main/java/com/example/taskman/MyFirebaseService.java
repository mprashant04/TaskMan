package com.example.taskman;

import com.example.taskman.common.Logs;
import com.example.taskman.common.Tasker;
import com.example.taskman.utils.DateUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

import lombok.NonNull;

public class MyFirebaseService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //firebase push notification received....
        super.onMessageReceived(remoteMessage);
        logMessage(remoteMessage);
        Tasker.sendData(this, remoteMessage.getData().get("title"), remoteMessage.getData().get("data"));
    }

    private void logMessage(RemoteMessage remoteMessage) {
        Date sentOn = DateUtils.stringToDate("yyyy/MM/dd HH:mm:ss", remoteMessage.getData().get("sentOn"));

        boolean warn = (DateUtils.diffInSeconds(sentOn) > 5);

        String msg = "FCM received "
                + "(" + DateUtils.diffInSeconds(sentOn) + "s)"
                + ": "
                + remoteMessage.getData();

        if (warn)
            Logs.warn(msg);
        else
            Logs.info(msg);
    }
}
