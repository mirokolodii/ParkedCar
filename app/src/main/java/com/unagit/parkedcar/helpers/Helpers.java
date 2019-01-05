package com.unagit.parkedcar.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import com.unagit.parkedcar.BuildConfig;
import com.unagit.parkedcar.R;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.core.app.NotificationCompat;

/**
 * Helper methods, used throughout the application.
 */
public class Helpers {

    /**
     * Shows toast message.
     * @param text
     * @param context
     */
    public static void showToast(String text, Context context) {
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, text, duration).show();
    }

    /**
     * Calculates difference between current time and parkedTimestamp.
     * @param parkedTimestamp Long Timestamp.
     * @return String, representing time difference in days, hours and minutes.
     */
    public static String timeDifference(Long parkedTimestamp) {
        Long currentTimestamp = Calendar.getInstance().getTimeInMillis();
        long diff = currentTimestamp - parkedTimestamp;

        /*
         * Convert time difference into string of format "x day(s), y hour(s), z min(s)".
         */
//        long diffSeconds = diff / 1000 % 60;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffMinutes = diff / (60 * 1000) % 60;
        // Put all parts of difference (days, hours and minutes) into an array.
        ArrayList<DurationPart> durationParts = new ArrayList<>();
        durationParts.add(new DurationPart(diffDays, "day"));
        durationParts.add(new DurationPart(diffHours, "hour"));
        durationParts.add(new DurationPart(diffMinutes, "minute"));

        // Get a sub array with values = 0
        ArrayList<DurationPart> zeroValueDurationParts = new ArrayList<>();
        for (DurationPart part : durationParts) {
            if(part.getValue() == 0) {
                zeroValueDurationParts.add(part);
            }
        }

        // Remove members with value = 0 from main array
        durationParts.removeAll(zeroValueDurationParts);

        // Combine all parts into one string and separate them with comma.
        String parkingDuration = "";
        for(int i=0; i<durationParts.size(); i++) {
            DurationPart part = durationParts.get(i);
            parkingDuration += part.getDuration();
            if(i < durationParts.size()-1) { // We don't want comma after last part
                parkingDuration += ", ";
            }
        }

        /*
         * Returns '1 min' text in case, when time since parking is less than 1 min.
         * Otherwise returns text, informing number of days and time since parking.
         */
        return parkingDuration.isEmpty() ? "1 min" : parkingDuration;
    }

    public static Notification getForegroundNotification(Context context) {

        // Register notification channel for Android version >= Android.O
        NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Constants.Notifications.NOTIFICATION_CHANNEL_ID,
                    Constants.Notifications.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT // Should be lower priority to not show it in a bar?
            );
            notificationChannel.setDescription(Constants.Notifications.NOTIFICATION_CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(false);

            try {
                notificationManager.createNotificationChannel(notificationChannel);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        return new NotificationCompat.Builder(context, Constants.Notifications.NOTIFICATION_CHANNEL_ID)
                        .setContentTitle("title")
                        .setContentText("text")
                        .setSmallIcon(R.drawable.ic_parking_icon)
                        .setTicker("ticker")
                        .build();
    }
}