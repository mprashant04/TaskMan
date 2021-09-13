package com.example.taskman.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import static android.content.Context.VIBRATOR_SERVICE;

public class Utils {

    public static void vibrate(Context context) {
        vibrate(context, 50);
    }


    public static int getSpinnerIndex(Spinner spinner, String stringToSearch) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(stringToSearch)) {
                return i;
            }
        }

        return 0;
    }

    public static void vibrate(Context context, int milliSec) {
        Vibrator myVib = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        myVib.vibrate(milliSec);
    }

    public static void toast(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void playTone(int toneType, int durationMs) {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_RING, 100);
        toneGen1.startTone(toneType, durationMs);
    }

    public static void toastNew(Activity activity, String msg) {
        toastNew(activity, msg, -1, true);
    }

    public static void toastNew(Activity activity, String msg, boolean longDuration) {
        toastNew(activity, msg, -1, longDuration);
    }

    public static void toastNew(Activity activity, String msg, @DrawableRes int imageId, boolean longDuration) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout_root));

        ImageView image = (ImageView) layout.findViewById(R.id.image);
        if (imageId > 0) image.setImageResource(imageId);


        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
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

    public static void infoDialog(Context context, String title) {
        infoDialog(context, title, "");
    }

    public static void infoDialog(Context context, String title, String message) {
        showDialog(context, title, message, android.R.drawable.ic_dialog_info);
    }

    public static void alertDialog(Context context, String title) {
        alertDialog(context, title, "");
    }

    public static void alertDialog(Context context, String title, String message) {
        showDialog(context, title, message, android.R.drawable.ic_dialog_alert);
    }

    private static void showDialog(Context context, String title, String message, @DrawableRes int iconId) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                //.setPositiveButton(android.R.string.ok, null)
                .setIcon(iconId)
                .show();
    }
}
