package com.example.taskman.brightness;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.example.taskman.MainActivity;
import com.example.taskman.common.AppConfig;
import com.example.taskman.utils.BrightnessUtils;
import com.example.taskman.utils.DialogUtils;

public class BrightnessService extends Service {

    BrightnessService THIS = null;


    ContentObserver contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);

            if (THIS != null)
                THIS.handle(brightness, selfChange);
        }
    };

    private void handle(int brightness, boolean selfChange) {
        if (AppConfig.getBrightnessShowToastDebugMessagesOnChange())
            DialogUtils.toast("Brightness  " + brightness, this);

        if (brightness < AppConfig.getBrightnessAutoMinimumValue() && BrightnessUtils.isAutoBrightnessEnabled(this))
            BrightnessUtils.setBrightness(this, AppConfig.getBrightnessAutoMinimumValue());
    }


    public static void init(MainActivity mainActivity) {
        mainActivity.startService(new Intent(mainActivity, BrightnessService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        THIS = this;
        DialogUtils.toastLong("Brightness service started...", this);
        getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false, contentObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(contentObserver);
    }
}
