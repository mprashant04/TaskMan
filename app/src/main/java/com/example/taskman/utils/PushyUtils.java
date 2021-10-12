package com.example.taskman.utils;

import android.app.Activity;
import android.content.Context;

import com.example.taskman.common.Logs;

import me.pushy.sdk.Pushy;

public class PushyUtils {
    private static String token = "--not-yet-populated--";
    private static boolean initiated = false;

    public static synchronized void register(Context context) {

        if (initiated) return;
        initiated = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Logs.info("Registering pushy...");
                    String t = Pushy.register(context);
                    if (!t.equals(token)) {
                        token = t;
                        Logs.info("Pushy token = " + token);
                    }
                } catch (Throwable ex) {
                    Logs.error("Pushy initiation failed!!");
                    Logs.error(ex);
                }
            }
        }).start();


    }

    public static void showToken(Activity context) {


        UserFeedbackUtils.vibrate(context, 200);
        Utils.copyToClipboard(context, token);
        DialogUtils.toastNew(context, "Pushy Token copied to clipboard. Copy to KiteConf GD sheet if it's been changed..\n\n" + token, true);


    }
}
