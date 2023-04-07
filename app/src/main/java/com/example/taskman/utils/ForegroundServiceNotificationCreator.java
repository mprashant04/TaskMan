package com.example.taskman.utils;

import android.app.Notification;
import android.app.Service;
import android.content.Context;

import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_NON_AUDIO_ALERT;
import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_TASK;

//created this singleton for using single notification for multiple foreground services
public class ForegroundServiceNotificationCreator {

    private static final int NOTIFICATION_ID = 1094;
    private static Notification notification = null;

        public static void startForeground(Service service) {
        service.startForeground(getNotificationId(), getNotification(service));
    }

    public static synchronized Notification getNotification(Context context) {
        if (notification == null) {
            //Intent notificationIntent = new Intent(context, MainActivity.class);
            //        PendingIntent pendingIntent =
            //                PendingIntent.getActivity(context, 0, notificationIntent,
            //                        PendingIntent.FLAG_IMMUTABLE);

            notification =
                    new Notification.Builder(context, NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE)
                            .setContentTitle("Title")
                            .setContentText("text")
                            //.setSmallIcon(R.drawable.icon)
                            //.setContentIntent(pendingIntent)
                            .setTicker("tick")
                            .build();
        }
        return notification;
    }

    private static int getNotificationId() {
        return NOTIFICATION_ID;
    }


}


