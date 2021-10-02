package com.example.taskman.utils;

import android.content.Context;

import com.example.taskman.common.Logs;
import com.example.taskman.task_handlers.NotificationHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.NonNull;

public class FireBaseUtils {

    private static boolean initiated = false;

    public static void init(Context context) {
        //testFireBase(context);
        if (!initiated) {
            subscribeTopic(context);
            initiated = true;
        }
    }

    private static void subscribeTopic(Context context) {
        Logs.info("FCM: ********  Subscribing Topic  ********");
        FirebaseMessaging.getInstance().subscribeToTopic("push_data")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Logs.error("FCM: Topic subscription failed!! " + task.getException());
                            NotificationHandler.showError(context, "Firebase topic subscription failed!!");
                        }
                        Logs.info("FCM: Subscribed topic");
                        DialogUtils.toastLong("Firebase topic subscribed...", context);
                    }
                });

    }

    private static void testFireBaseToken(Context context) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            DialogUtils.toastLong("getInstanceId failed: " + task.getException().getMessage(), context);
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();


                        DialogUtils.toastLong("Token: " + token, context);
                    }
                });
    }

}
