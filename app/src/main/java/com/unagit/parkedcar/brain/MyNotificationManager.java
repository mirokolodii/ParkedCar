package com.unagit.parkedcar.brain;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.unagit.parkedcar.R;
import com.unagit.parkedcar.activities.MainActivity;
import com.unagit.parkedcar.helpers.Constants;
import com.unagit.parkedcar.services.NotificationActionHandlerService;
import java.util.Locale;
import static com.unagit.parkedcar.activities.MainActivity.LOG_TAG;

/**
 * Creates and displays notification with parked car information.
 */

public class MyNotificationManager {
    public MyNotificationManager() {}

    /**
     * Creates and displays notification with corresponding actions:
     * MAP - shows parking location in Google Maps;
     * DIRECTIONS - opens directions dialog in Google Maps;
     * CLEAR - removes notification and parking location.
     * @param context required for notification creation.
     * @param location if provided, gets accuracy and shows it in notification.
     */
    public void sendNotification(Context context, @Nullable Location location) {
        MyDefaultPreferenceManager preferenceManager = new MyDefaultPreferenceManager(context);

        // Check user's preferences regarding notification sending
        if(!preferenceManager.shouldSendNotification()) {
            return;
        }

        NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Set notifications channel for Android.O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Constants.Notifications.CHANNEL_ID,
                    Constants.Notifications.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationChannel.setDescription(Constants.Notifications.CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(false);

            try {
                notificationManager.createNotificationChannel(notificationChannel);
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Error while setting Notification Channel for Notification Manager: " + e.toString());
            }
        }

        String accuracy = "";
        if(location != null) {
            accuracy = String.format(Locale.getDefault(), " %s: %.2f %s",
                    context.getString(R.string.accuracy_text),
                    location.getAccuracy(),
                    context.getString(R.string.accuracy_unit));
        }
        // This intent is triggered on notification click
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent =
                PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.Notifications.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_parking_icon)
                .setContentTitle(context.getString(R.string.notification_title))
                .setSubText(accuracy)
                .setContentText(context.getString(R.string.notification_text))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(Color.GREEN)
                .setContentIntent(mainActivityPendingIntent);
        //.setAutoCancel(true); // Clear notification automatically at notification click. Works only if setContentIntent is specified

        // Add Actions
        mBuilder
                .addAction(android.R.drawable.ic_menu_mylocation,
                        context.getString(R.string.notification_action_maps),
                        getPendingIntent(context, Constants.Notifications.ACTION_SHOW_ON_MAP))
                .addAction(android.R.drawable.ic_menu_directions,
                        context.getString(R.string.notification_action_directions),
                        getPendingIntent(context, Constants.Notifications.ACTION_DIRECTIONS))
                .addAction(android.R.drawable.ic_notification_clear_all,
                        context.getString(R.string.notification_action_clear),
                        getPendingIntent(context, Constants.Notifications.ACTION_CLEAR));

        // Send notification
        try {
            notificationManager.notify(Constants.Notifications.NOTIFICATION_ID, mBuilder.build());
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "NotificationManager.notify throws an exception: " + e.getMessage());
        }
    }

    // Create PendingIntent for notification's action
    private PendingIntent getPendingIntent(Context context, String action) {
        Intent notificationAction = new Intent(context, NotificationActionHandlerService.class);
        notificationAction.setAction(action);
        return PendingIntent.getService(context, 0, notificationAction, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}