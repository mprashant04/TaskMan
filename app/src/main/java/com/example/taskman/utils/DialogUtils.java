package com.example.taskman.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.example.taskman.R;

public class DialogUtils {

    public static void toast(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void toastNew(Activity activity, String msg) {
        toastNew(activity, msg, -1, true);
    }

    public static void toastNew(Activity activity, String msg, @ColorRes int colorId) {
        toastNew(activity, msg, -1, true, colorId);
    }

    public static void toastNew(Activity activity, String msg, boolean longDuration) {
        toastNew(activity, msg, -1, longDuration);
    }

    public static void toastNew(Activity activity, String msg, boolean longDuration, @ColorRes int colorId) {
        toastNew(activity, msg, -1, longDuration, colorId);
    }

    public static void toastNew(Activity activity, String msg, @DrawableRes int imageId, boolean longDuration) {
        toastNew(activity, msg, imageId, longDuration, R.color.toast_background_yellow);
    }


    public static void toastNew(Activity activity, String msg, @DrawableRes int imageId, boolean longDuration, @ColorRes int colorId) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout_root));

        //----  change background color  ---------
        Drawable background = layout.getBackground();
        //msg = msg + "-" + background.getClass();
        if (background instanceof ShapeDrawable) {
            ShapeDrawable shapeDrawable = (ShapeDrawable) background;
            shapeDrawable.getPaint().setColor(ContextCompat.getColor(activity, colorId));
        } else if (background instanceof GradientDrawable) {
            // cast to 'GradientDrawable'
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(ContextCompat.getColor(activity, colorId));
        } else if (background instanceof ColorDrawable) {
            // alpha value may need to be set again after this call
            ColorDrawable colorDrawable = (ColorDrawable) background;
            colorDrawable.setColor(ContextCompat.getColor(activity, colorId));
        }


        ImageView image = (ImageView) layout.findViewById(R.id.image);
        if (imageId > 0) image.setImageResource(imageId);


        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);

        if (colorId == R.color.toast_background_red)
            text.setTextColor(activity.getResources().getColor(R.color.white));  //to maintain background/foreground contrast

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
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
