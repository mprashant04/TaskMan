package com.example.taskman.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;

import static android.content.Context.VIBRATOR_SERVICE;

public class UserFeedbackUtils {


    public static void vibrate(Context context) {
        vibrate(context, 50);
    }

    public static void vibrate(Context context, int milliSec) {
        new Thread(() -> {
            Vibrator myVib = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            myVib.vibrate(milliSec);
        }).start();
    }


    public static void playTone(int toneType, int durationMs) {
        new Thread(() -> {
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_RING, 100);
            toneGen1.startTone(toneType, durationMs);
        }).start();
    }

}
