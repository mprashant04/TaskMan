package com.example.taskman.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.example.taskman.common.Logs;

public class BrightnessUtils {
    public static final int BRIGHTNESS_MAX = 225;
    public static final int BRIGHTNESS_MIN = 0;


    public static void setBrightness(Context context, int brightness) {

        if (brightness < BRIGHTNESS_MIN)
            brightness = BRIGHTNESS_MIN;
        else if (brightness > BRIGHTNESS_MAX)
            brightness = BRIGHTNESS_MAX;

        ContentResolver cResolver = context.getContentResolver();
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);

    }

    public static boolean isAutoBrightnessEnabled(Context context) {
        try {
            int brightnessmode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            return brightnessmode == 1;
        } catch (Throwable e) {
            Logs.error(e);
            return false;
        }

    }
}