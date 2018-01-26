package com.unagit.parkedcar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by a264889 on 27.01.2018.
 */

public class MyDefaultPreferenceManager {
    private static SharedPreferences spref;

    //Keys for SharedPreferences
    static final String PARKING_LOCATION_LATITUDE = "parkingLocationLatitude";
    static final String PARKING_LOCATION_LONGITUDE = "parkingLocationLongitude";
    static final String DEVICE_ADDRESSES = "deviceAddresses";


    MyDefaultPreferenceManager(Context context) {
        spref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Check whether key exists in SharedPreferences
    boolean isSet(String key) {
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
        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        } else {
            Log.e(MainActivity.LOG_TAG, "Incorrect value type is passed to setValue method");
            return;
        }
        // Apply changes to editor
        editor.apply();
    }

    Float getLatitude() {
        return spref.getFloat(PARKING_LOCATION_LATITUDE, -1);
    }

    Float getLongitude() {
        return spref.getFloat(PARKING_LOCATION_LONGITUDE, -1);
    }

    Set<String> getDevices() {
        Set<String> s = new HashSet<>(); // default value
        return spref.getStringSet(DEVICE_ADDRESSES, s);
    }

    void removeLocation() {
        if (isSet(PARKING_LOCATION_LATITUDE) & isSet(PARKING_LOCATION_LONGITUDE)) {
            SharedPreferences.Editor editor = spref.edit();
            editor.remove(PARKING_LOCATION_LATITUDE).remove(PARKING_LOCATION_LONGITUDE);
            editor.apply();
        }
    }
}
