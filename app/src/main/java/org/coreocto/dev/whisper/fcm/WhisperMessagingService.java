package org.coreocto.dev.whisper.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.coreocto.dev.whisper.Constants;
import org.coreocto.dev.whisper.R;
import org.coreocto.dev.whisper.activity.UsersActivity;

import java.util.Map;
import java.util.Set;

/**
 * Created by John on 3/20/2018.
 */

public class WhisperMessagingService extends FirebaseMessagingService {
    private static final String TAG = "WhisperMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Set<String> keys = remoteMessage.getData().keySet();
            for (String s : keys) {
                Log.d(TAG, "fcm data:" + remoteMessage.getData().get(s));
            }

//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }

            Map<String, String> data = remoteMessage.getData();
            String content = data.get("content");
            String from = null;
            if (content.indexOf("<%>") > -1) {
                String[] dataArr = content.split("<%>");
                content = dataArr[0];
                from = dataArr[1];
            }
            data.get("from");

            sendNotification(content, from);
        }

        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            sendNotification(remoteMessage.getNotification().getBody());
//        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

//    private void scheduleJob() {
//    }
//
//    private void handleNow() {
//    }

    private void sendNotification(String messageBody, String messageFrom) {

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser == null) {
            return;
        }

        Context ctx = getApplicationContext();

        NotificationManager notification_manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notification_builder = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String chanel_id = "3000";
            CharSequence name = "Channel Name";
            String description = "Chanel Description";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(chanel_id, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notification_manager.createNotificationChannel(mChannel);
            notification_builder = new NotificationCompat.Builder(ctx, chanel_id);
        } else {
            notification_builder = new NotificationCompat.Builder(ctx);
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(ctx, UsersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (messageFrom != null) {
            intent.putExtra(Constants.EXTRA_FROM, messageFrom);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); //the last parameter must set to FLAG_UPDATE_CURRENT, otherwise the activity will not receive the "extra"

        notification_builder
                .setSmallIcon(R.drawable.if_voice)  //DO NOT skip this icon, without it the notification will not show up
                .setContentTitle("New message from Whisper")
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // notificationId is a unique int for each notification that you must define
        notification_manager.notify((int) Math.random(), notification_builder.build());
    }
}
