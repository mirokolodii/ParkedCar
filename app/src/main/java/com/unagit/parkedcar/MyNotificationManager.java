package com.unagit.parkedcar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static com.unagit.parkedcar.Constants.Notifications.NOTIFICATION_TEXT;
import static com.unagit.parkedcar.MainActivity.LOG_TAG;

/**
 * Created by a264889 on 28.01.2018.
 */

public class MyNotificationManager {
    public MyNotificationManager() {}
    void sendNotification(Context context, @Nullable Location location) {

        String text = NOTIFICATION_TEXT;
        if(location != null) {
            text += " Accuracy: " + location.getAccuracy() + "m.";
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.Requests.NOTIFICATIONS_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_more)
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
        NotificationManager mNotificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            mNotificationManager.notify(Constants.Requests.NOTIFICATION_ID, mBuilder.build());
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "NotificationManager notify method throws an exception" + e.getMessage());
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