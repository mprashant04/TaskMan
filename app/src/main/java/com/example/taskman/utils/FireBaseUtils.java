//package com.example.taskman.utils;
//
//import android.app.Activity;
//
//import com.example.taskman.common.Logs;
//
//import me.pushy.sdk.Pushy;
//
////import com.google.android.gms.tasks.OnCompleteListener;
////import com.google.android.gms.tasks.Task;
////import com.google.firebase.iid.FirebaseInstanceId;
////import com.google.firebase.iid.InstanceIdResult;
////import com.google.firebase.messaging.FirebaseMessaging;
//
//public class FireBaseUtils {
//
//    private static String token = "";
//
//    private static boolean initiated = false;
//
////    public static void init(Activity activity) {
////        if (!initiated) {
////            //subscribeTopic(context);
////            getFireBaseToken(activity, true);
////            initiated = true;
////        }
////    }
//
//
////    private static void subscribeTopic(Context context) {
////        Logs.info("FCM: ********  Subscribing Topic  ********");
////        FirebaseMessaging.getInstance().subscribeToTopic("push_data")
////                .addOnCompleteListener(new OnCompleteListener<Void>() {
////                    @Override
////                    public void onComplete(@NonNull Task<Void> task) {
////                        if (!task.isSuccessful()) {
////                            Logs.error("FCM: Topic subscription failed!! " + task.getException());
////                            NotificationHandler.showError(context, "Firebase topic subscription failed!!");
////                        }
////                        Logs.info("FCM: Subscribed topic");
////                        DialogUtils.toastLong("Firebase topic subscribed...", context);
////                    }
////                });
////
////    }
//
//    public static void getFireBaseToken(Activity context) {
////        FirebaseInstanceId.getInstance().getInstanceId()
////                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
////                    @Override
////                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<InstanceIdResult> task) {
////                        if (!task.isSuccessful()) {
////                            DialogUtils.toastLong("FCM token get failed: " + task.getException().getMessage(), context);
////                            return;
////                        }
////
////                        String token = task.getResult().getToken();
////
////                        UserFeedbackUtils.vibrate(context, 200);
////                        Utils.copyToClipboard(context, token);
////                        DialogUtils.toastNew(context, "FCM Token copied to clipboard. Copy to KiteConf GD sheet if it's been changed..\n\n" + token, true);
////                        //DialogUtils.toast("FCM Token: \n\n" + token, context);
////                    }
////                });
//
//        try {
//            token = Pushy.register(context);
//
//            if (!silent) {
//                UserFeedbackUtils.vibrate(context, 200);
//                Utils.copyToClipboard(context, token);
//                DialogUtils.toastNew(context, "Pushy Token copied to clipboard. Copy to KiteConf GD sheet if it's been changed..\n\n" + token, true);
//            }
//
//        } catch (Throwable ex) {
//            Logs.error("Pushy initiation failed!!");
//            Logs.error(ex);
//        }
//    }
//
//
//    public static void getFireBaseToken(Activity context, boolean silent) {
////        FirebaseInstanceId.getInstance().getInstanceId()
////                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
////                    @Override
////                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<InstanceIdResult> task) {
////                        if (!task.isSuccessful()) {
////                            DialogUtils.toastLong("FCM token get failed: " + task.getException().getMessage(), context);
////                            return;
////                        }
////
////                        String token = task.getResult().getToken();
////
////                        UserFeedbackUtils.vibrate(context, 200);
////                        Utils.copyToClipboard(context, token);
////                        DialogUtils.toastNew(context, "FCM Token copied to clipboard. Copy to KiteConf GD sheet if it's been changed..\n\n" + token, true);
////                        //DialogUtils.toast("FCM Token: \n\n" + token, context);
////                    }
////                });
//
//        try {
//            token = Pushy.register(context);
//
//            if (!silent) {
//                UserFeedbackUtils.vibrate(context, 200);
//                Utils.copyToClipboard(context, token);
//                DialogUtils.toastNew(context, "Pushy Token copied to clipboard. Copy to KiteConf GD sheet if it's been changed..\n\n" + token, true);
//            }
//
//        } catch (Throwable ex) {
//            Logs.error("Pushy initiation failed!!");
//            Logs.error(ex);
//        }
//    }
//}
