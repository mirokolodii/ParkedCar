package com.unagit.parkedcar;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by a264889 on 27.01.2018.
 */

public class MyDefaultPreferenceManager {
    private static SharedPreferences spref;


    MyDefaultPreferenceManager(Context context) {
        spref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Check whether key exists in SharedPreferences
    private boolean isSet(String key) {
        return spref.contains(key);
    }

    /**
     * Saves (key, value) pair into DefaultSharedPreferences
     */
    void setValue(String key, Object value) {
        // Get instance of SharedPreferences editor
        SharedPreferences.Editor editor = spref.edit();
        // Get type of value and put into editor
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if(value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if(value instanceof Long) {
            editor.putLong(key, (Long) value);
        }
        else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        } else {
            Log.e(MainActivity.LOG_TAG, "Incorrect value type is passed to setValue method");
            return;
        }
        // Apply changes to editor
        editor.apply();
    }

    void saveLocation(Location location) {
        setValue(Constants.Store.PARKING_LOCATION_LATITUDE, (float) location.getLatitude());
        setValue(Constants.Store.PARKING_LOCATION_LONGITUDE, (float) location.getLongitude());
        setValue(Constants.Store.IS_PARKED, true);
        setValue(Constants.Store.PARKED_TIME, getCurrentTimestamp());
    }

    void setParkedAutomatically(boolean value) {
        setValue(Constants.Store.IS_PARKED_AUTOMATICALLY, value);
    }

    Boolean isParked() {
        return (isSet(Constants.Store.IS_PARKED)
                && spref.getBoolean(Constants.Store.IS_PARKED, false));
    }

    Boolean isParkedAutomatically() {
        return (isSet(Constants.Store.IS_PARKED_AUTOMATICALLY)
                && spref.getBoolean(Constants.Store.IS_PARKED_AUTOMATICALLY, false));
    }

    Long getTimestamp() {
        return spref.getLong(Constants.Store.PARKED_TIME, 0);
    }

    Float getLatitude() {
        return spref.getFloat(Constants.Store.PARKING_LOCATION_LATITUDE, -1);
    }

    Float getLongitude() {
        return spref.getFloat(Constants.Store.PARKING_LOCATION_LONGITUDE, -1);
    }

    Set<String> getDevices() {
        Set<String> s = new HashSet<>(); // default value
        return spref.getStringSet(Constants.Store.DEVICE_ADDRESSES, s);
    }

    void removeLocation() {
        if (isSet(Constants.Store.PARKING_LOCATION_LATITUDE)
                && isSet(Constants.Store.PARKING_LOCATION_LONGITUDE)
                && isSet(Constants.Store.PARKED_TIME)
                && isSet(Constants.Store.IS_PARKED_AUTOMATICALLY)
                && isSet(Constants.Store.IS_PARKED)) {
            SharedPreferences.Editor editor = spref.edit();
            editor
                    .remove(Constants.Store.PARKING_LOCATION_LATITUDE)
                    .remove(Constants.Store.PARKING_LOCATION_LONGITUDE)
                    .remove(Constants.Store.IS_PARKED)
                    .remove(Constants.Store.IS_PARKED_AUTOMATICALLY)
                    .remove(Constants.Store.PARKED_TIME);
            editor.apply();
        }
    }

    private Long getCurrentTimestamp() {
              return Calendar.getInstance().getTimeInMillis();
    }
}
