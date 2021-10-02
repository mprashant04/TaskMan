package com.example.taskman.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Spinner;

import com.example.taskman.EditTaskActivity;

import java.io.File;
import java.text.SimpleDateFormat;

import static android.content.Context.POWER_SERVICE;

public class Utils {


    public static String getAppBuildTimeStamp(Context context) {
        try {
            String timeStamp = "";

            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            String appFile = appInfo.sourceDir;
            long time = new File(appFile).lastModified();

            SimpleDateFormat formatter = new SimpleDateFormat("d-MMM-yyyy HH:mm");
            timeStamp = formatter.format(time);

            return timeStamp;
        } catch (Throwable ex) {
            return "Error while getting APK build date!!!";
        }
    }

    public static int getSpinnerIndex(Spinner spinner, String stringToSearch) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(stringToSearch)) {
                return i;
            }
        }
        return 0;
    }


    public static void checkExternalStorageAccess(Context context) {
        if (Environment.isExternalStorageManager()) {
            //todo when permission is granted
        } else {
            //request for the permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }

    public static Intent createEditTaskIntent(Context context, long taskId, String calledFrom) {
        Intent intent = new Intent(context, EditTaskActivity.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("calledFrom", calledFrom);
        return intent;
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }

    public static synchronized boolean isBatteryOptimizationEnabled(Context context) {
        Intent intent = new Intent();
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        return !pm.isIgnoringBatteryOptimizations(packageName);
    }

}
