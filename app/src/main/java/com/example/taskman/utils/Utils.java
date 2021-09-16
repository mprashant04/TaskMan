package com.example.taskman.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import com.example.taskman.EditTaskActivity;
import com.example.taskman.R;

import java.io.File;
import java.text.SimpleDateFormat;

import static android.content.Context.VIBRATOR_SERVICE;

public class Utils {


    public static String getAppBuildTimeStamp(Context context)  {
        try {
            String timeStamp = "";

            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            String appFile = appInfo.sourceDir;
            long time = new File(appFile).lastModified();

            SimpleDateFormat formatter = new SimpleDateFormat("d-MMM-yyyy HH:mm");
            timeStamp = formatter.format(time);

            return timeStamp;
        }
        catch (Throwable ex){
            return "Error while getting APK build date!";
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

}
