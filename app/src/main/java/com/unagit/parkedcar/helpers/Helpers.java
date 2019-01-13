package com.unagit.parkedcar.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import com.unagit.parkedcar.R;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import androidx.core.app.NotificationCompat;

/**
 * Helper methods, used throughout the application.
 */
public class Helpers {

    /**
     * Shows toast message.
     *
     * @param text
     * @param context
     */
    public static void showToast(String text, Context context) {
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, text, duration).show();
    }

    /**
     * Calculates difference between current time and parkedTimestamp.
     *
     * @param parkedTimestamp Long Timestamp.
     * @param context which is used to get string resources.
     * @return String, representing time difference in days, hours and minutes.
     */
    public static String getTimeDifference(Long parkedTimestamp, Context context) {
        Long currentTimestamp = Calendar.getInstance().getTimeInMillis();
        long diff = currentTimestamp - parkedTimestamp;

        // Get time units from timestamp
//        long diffSeconds = diff / 1000 % 60;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffMinutes = diff / (60 * 1000) % 60;

        // Create a map of time units combined with unit names
        Map<Long, String> timeUnits = new LinkedHashMap<>();
        putIfNotZero(timeUnits, diffDays, context.getString(R.string.parking_time_day));
        putIfNotZero(timeUnits, diffHours, context.getString(R.string.parking_time_hour));
        putIfNotZero(timeUnits, diffMinutes, context.getString(R.string.parking_time_min));

        // If less than 1 min passed since parking, return 1 min.
        if(timeUnits.isEmpty()) {
            return String.format(Locale.getDefault(),
                    "%d %s %s",
                    1,
                    context.getString(R.string.parking_time_min),
                    context.getString(R.string.parking_time_parked_ago));
        }

        // Merge all units into single string and separate with comma
        StringBuilder parkingDuration = new StringBuilder();
        Iterator<Map.Entry<Long, String>> iterator = timeUnits.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Long, String> pair = iterator.next();
            parkingDuration.append(String.format(Locale.getDefault(),
                    "%d %s", pair.getKey(), pair.getValue()));
            // Separate each unit with comma
            if(iterator.hasNext()) {
                parkingDuration.append(", ");
            }
        }
        parkingDuration.append(context.getString(R.string.parking_time_parked_ago));
        return parkingDuration.toString();
    }

    /**
     * Puts new Map.Entry into a map only in case key is not equal to zero.
     * @param map to which a new entry should be added
     * @param key on an antry to be added to map, only if it's not equal to zero
     * @param value of an entry to be added to map
     */
    private static void putIfNotZero(Map<Long,String> map, Long key, String value) {
        if(key != 0) {
            map.put(key, value);
        }
    }

    public static Notification getForegroundNotification(Context context) {

        // Register notification channel for Android version >= Android.O
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
                e.printStackTrace();
            }
        }

        return new NotificationCompat.Builder(context, Constants.Notifications.CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.foreground_notification_text))
                .setSmallIcon(R.drawable.ic_parking_icon)
                .setColor(Color.GREEN)
                .build();
    }
}