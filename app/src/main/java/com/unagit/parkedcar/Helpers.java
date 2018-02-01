package com.unagit.parkedcar;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import static com.unagit.parkedcar.MainActivity.LOG_TAG;

/**
 * Created by a264889 on 27.01.2018.
 */

public class Helpers {
    public static void showToast(String text, Context context) {
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
    }

    public static String timeDifference(String parkTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sParkDateTime2 = "2018-01-31 19:01:00";
        String sCurrentDateTime = format.format(new Date());

        try {
            // Get dates from strings, using format
            Date parkDateTime = format.parse(sParkDateTime2);
            Date currentDateTime = format.parse(sCurrentDateTime);
            // Calculate difference in two dates
            long diff = currentDateTime.getTime()-parkDateTime.getTime();
//            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);
//            Log.d(LOG_TAG, "Millisec: " + diff);
//            Log.d(LOG_TAG, "Sec: " + diffSeconds);
//            Log.d(LOG_TAG, "Minutes: " + diffMinutes);
//            Log.d(LOG_TAG, "Hours: " + diffHours);
//            Log.d(LOG_TAG, "Days: " + diffDays);
            // Build return string
            String parkingDuration = "";
            if(diffDays == 1) {
                parkingDuration += diffDays + " day, ";
            } else if(diffDays > 0) {
                parkingDuration += diffDays + " days, ";
            }

            if(diffHours == 1) {
                parkingDuration += diffHours + " hour, ";
            } else if(diffDays > 0) {
                parkingDuration += diffHours + " hours, ";
            }

            if(diffMinutes == 1) {
                parkingDuration += diffMinutes + " minute";
            } else if(diffDays > 0) {
                parkingDuration += diffMinutes + " minutes";
            }
            return parkingDuration;

        } catch (ParseException e) {
            Log.e(LOG_TAG, e.toString());
        }
        return ""; // Return empty string
    }
}
