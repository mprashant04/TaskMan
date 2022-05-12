package com.example.taskman.common;

import android.content.Context;
import android.content.Intent;

public class Tasker {

    public static synchronized void sendData(Context context, String title, String data) {
        Intent intent = new Intent("com.example.taskman.firebase_push_notification");
        intent.putExtra("data", data);
        intent.putExtra("title", title);
        context.sendBroadcast(intent);
    }
}
