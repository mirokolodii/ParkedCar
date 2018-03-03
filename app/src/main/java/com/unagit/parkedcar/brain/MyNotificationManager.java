package com.unagit.parkedcar.brain;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.activities.MainActivity;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.services.NotificationActionHandlerService;
import java.util.Locale;
import static com.unagit.parkedcar.helpers.Constants.Notifications.NOTIFICATION_TEXT;
import static com.unagit.parkedcar.activities.MainActivity.LOG_TAG;

/**
 * Created by a264889 on 28.01.2018.
 */

public class MyNotificationManager {
    public MyNotificationManager() {}
    public void sendNotification(Context context, @Nullable Location location) {
        NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // ! Uncomment this section after targeting Android.O and above.
        // It is required for proper notifications work in SDK >= 26.

//        // Set notifications channel for Android.O and above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(
//                    Constants.Notifications.NOTIFICATION_CHANNEL_ID,
//                    Constants.Notifications.NOTIFICATION_CHANNEL_NAME,
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            notificationChannel.setDescription(Constants.Notifications.NOTIFICATION_CHANNEL_DESCRIPTION);
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.GREEN);
//            notificationChannel.enableVibration(false);
//
//            try {
//                notificationManager.createNotificationChannel(notificationChannel);
//            } catch (NullPointerException e) {
//                Log.e(LOG_TAG, "Error while setting Notification Channel for Notification Manager: " + e.toString());
//            }
//        }

        String text = NOTIFICATION_TEXT;
        String accuracy = "";
        if(location != null) {
            accuracy = String.format(Locale.getDefault(), " Accuracy: %.2f m.", location.getAccuracy());
        }
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent =
                PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Replace below rows in case targeting SDK >= 26, as constructor has changed for NotificationCompat.Builder
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.Notifications.NOTIFICATION_CHANNEL_ID)
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_parking_icon)
                .setContentTitle(Constants.Notifications.NOTIFICATION_TITLE)
                .setSubText(accuracy)
                .setContentText(text)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(Color.GREEN)
//                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0)); // Empty intent
                .setContentIntent(mainActivityPendingIntent);
        //.setAutoCancel(true); // Clear notification automatically at notification click. Works only if setContentIntent is specified

        // Add Actions
        mBuilder
                .addAction(android.R.drawable.ic_menu_mylocation, Constants.Notifications.NOTIFICATION_ACTION_TITLE_SHOW, getPendingIntent(context, Constants.Notifications.ACTION_SHOW_ON_MAP))
                .addAction(android.R.drawable.ic_menu_directions, Constants.Notifications.NOTIFICATION_ACTION_TITLE_DIRECTIONS, getPendingIntent(context, Constants.Notifications.ACTION_DIRECTIONS))
                .addAction(android.R.drawable.ic_notification_clear_all, Constants.Notifications.NOTIFICATION_ACTION_TITLE_CLEAR, getPendingIntent(context, Constants.Notifications.ACTION_CLEAR));

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
        PendingIntent pIntent =
                PendingIntent.getService(context, 0, notificationAction, PendingIntent.FLAG_UPDATE_CURRENT);
        return  pIntent;
    }
}