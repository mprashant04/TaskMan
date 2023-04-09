package com.example.taskman.utils;

import android.app.Notification;
import android.app.Service;
import android.content.Context;

import static com.example.taskman.common.Declarations.NOTIFICATION_CHANNEL_ID_FOREGROUND_SERVICE;


public class ForegroundServiceNotificationCreator {

    //using common id so that same notification be used for multiple foreground serivces
    //this number is taken from me.pushy.sdk.services.PushySocketService.java class, line 123.  So that both pushy and my own services will continue using same single notification
    private final static int NOTIFICATION_ID = 100031;
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
        //return NOTIFICATION_ID++;
    }


}


