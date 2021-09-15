package com.example.taskman.common;

import android.content.Context;
import android.content.Intent;

public class Tasker {

    public static synchronized void sendWatchNotification(Context context, String msg) {
        Intent intent = new Intent("com.example.taskman.watch_alert");
        intent.putExtra("message", msg);
        context.sendBroadcast(intent);
    }
}
