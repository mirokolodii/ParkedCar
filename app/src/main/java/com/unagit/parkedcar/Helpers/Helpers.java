package com.unagit.parkedcar.Helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.unagit.parkedcar.MainActivity.LOG_TAG;

/**
 * Created by a264889 on 27.01.2018.
 */

public class Helpers {
    public static void showToast(String text, Context context) {
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
    }

    /**
     * Method calculates difference between current moment in time and provided time.
     * @param parkTime String representing date and time in following format "yyyy-MM-dd HH:mm:ss".
     *                 Example: "2016-02-23 23:12:35"
     * @return String with difference in days, hours and minutes.
     */
    public static String timeDifference(String parkTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String sParkDateTime2 = "2018-01-30 12:31:00";
        String sCurrentDateTime = format.format(new Date());

        try {
            // Get dates from strings, using format
            Date parkDateTime = format.parse(sParkDateTime2);
            Date currentDateTime = format.parse(sCurrentDateTime);
            // Calculate difference between two dates
            long diff = currentDateTime.getTime()-parkDateTime.getTime();
//            long diffSeconds = diff / 1000 % 60;
            long diffDays = diff / (24 * 60 * 60 * 1000);
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffMinutes = diff / (60 * 1000) % 60;
            ArrayList<DurationPart> durationParts = new ArrayList<>();
            durationParts.add(new DurationPart(diffDays, "day"));
            durationParts.add(new DurationPart(diffHours, "hour"));
            durationParts.add(new DurationPart(diffMinutes, "minute"));

            // Remove those DurationParts with value == 0
            ArrayList<DurationPart> durationPartstoRemove = new ArrayList<>();
            for (DurationPart part : durationParts) {
                if(part.getValue() == 0) {
                    durationPartstoRemove.add(part);
                }
            }
            durationParts.removeAll(durationPartstoRemove);

            // Launch parts, separated with comma, together into one string
            String parkingDuration = "";
            for(int i=0; i<durationParts.size(); i++) {
                DurationPart part = durationParts.get(i);
                parkingDuration += String.format(Locale.getDefault(), "%d %s", part.getValue(), part.getText());
                if(i < durationParts.size()-1) { // We don't want comma after last part
                        parkingDuration += ", ";
                }
            }
            return parkingDuration;

        } catch (ParseException e) {
            Log.e(LOG_TAG, e.toString());
        }

        return ""; // Return empty string
    }
}

