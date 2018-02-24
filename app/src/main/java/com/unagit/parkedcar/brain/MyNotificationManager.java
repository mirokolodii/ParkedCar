package com.unagit.parkedcar.brain;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.unagit.parkedcar.R;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.services.NotificationActionHandlerService;

import static com.unagit.parkedcar.helpers.Constants.Notifications.NOTIFICATION_TEXT;
import static com.unagit.parkedcar.activities.MainActivity.LOG_TAG;

/**
 * Created by a264889 on 28.01.2018.
 */

public class MyNotificationManager {
    public MyNotificationManager() {}
    public void sendNotification(Context context, @Nullable Location location) {

        NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Set notifications channel for Android.O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Constants.Notifications.NOTIFICATION_CHANNEL_ID,
                    Constants.Notifications.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setDescription(Constants.Notifications.NOTIFICATION_CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(false);

            try {
                notificationManager.createNotificationChannel(notificationChannel);
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Error while setting Notification Channel for Notification Manager: " + e.toString());
            }

        }
        String text = NOTIFICATION_TEXT;
        if(location != null) {
            // TODO: Work on text. Hint: " += "
            text = " Accuracy: " + location.getAccuracy() + " m.";
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.Notifications.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_parking_icon)
                .setContentTitle(Constants.Notifications.NOTIFICATION_TITLE)
                .setContentText(text)
                .setOngoing(true)
                .setColor(Color.GREEN)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0)); // Empty intent
        //.setAutoCancel(true); // Clear notification automatically at notification click. Works only if setContentIntent is specified

        // Add Actions
        mBuilder
                .addAction(android.R.drawable.sym_action_chat, Constants.Notifications.NOTIFICATION_ACTION_TITLE_SHOW, getPendingIntent(context, Constants.Notifications.ACTION_SHOW_ON_MAP))
                .addAction(android.R.drawable.sym_action_chat, Constants.Notifications.NOTIFICATION_ACTION_TITLE_DIRECTIONS, getPendingIntent(context, Constants.Notifications.ACTION_DIRECTIONS))
                .addAction(android.R.drawable.sym_action_chat, Constants.Notifications.NOTIFICATION_ACTION_TITLE_CLEAR, getPendingIntent(context, Constants.Notifications.ACTION_CLEAR));

        // Send notification

        try {
            notificationManager.notify(Constants.Notifications.NOTIFICATION_ID, mBuilder.build());
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "NotificationManager.notify throws an exception: " + e.getMessage());
        }
    }

    // Create PendingIntent for action in notification
    private PendingIntent getPendingIntent(Context context, String action) {
        Intent notificationAction = new Intent(context, NotificationActionHandlerService.class);
        notificationAction.setAction(action);
        PendingIntent pIntent = PendingIntent.getService(context, 0, notificationAction, PendingIntent.FLAG_UPDATE_CURRENT);
        return  pIntent;
    }
}