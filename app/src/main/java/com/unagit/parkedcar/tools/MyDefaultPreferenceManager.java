package com.unagit.parkedcar.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.unagit.parkedcar.R;
import com.unagit.parkedcar.views.MainActivity;
import com.unagit.parkedcar.helpers.Constants;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class includes helper methods for DefaultSharedPreferences.
 */
public class MyDefaultPreferenceManager {
    private SharedPreferences spref;
    private Context context;

    public MyDefaultPreferenceManager(Context context) {
        this.context = context;
        spref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Verifies whether key exists in SharedPreferences.
      */
    private boolean isSet(String key) {
        return spref.contains(key);
    }

    /**
     * Saves (key, value) pair into DefaultSharedPreferences.
     */
    public void setValue(String key, Object value) {
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
        listPrefs();
    }

    /**
     * Saves location, current time and set IS_PARKED to true.
     */
    public void saveLocation(Location location) {
        setValue(Constants.Store.PARKING_LOCATION_LATITUDE, (float) location.getLatitude());
        setValue(Constants.Store.PARKING_LOCATION_LONGITUDE, (float) location.getLongitude());
        setValue(Constants.Store.IS_PARKED, true);
        setValue(Constants.Store.PARKED_TIME, getCurrentTimestamp());
    }

    /**
     * Parking type.
     * @param value identifies, whether car has been parked automatically (via Bluetooth) or manually.
     */
    public void setParkedAutomatically(boolean value) {
        setValue(Constants.Store.IS_PARKED_AUTOMATICALLY, value);
    }

    public Boolean isParked() {
        return (isSet(Constants.Store.IS_PARKED)
                && spref.getBoolean(Constants.Store.IS_PARKED, false));
    }

    public Boolean isParkedAutomatically() {
        return (isSet(Constants.Store.IS_PARKED_AUTOMATICALLY)
                && spref.getBoolean(Constants.Store.IS_PARKED_AUTOMATICALLY, false));
    }

    public Long getTimestamp() {
        return spref.getLong(Constants.Store.PARKED_TIME, 0);
    }

    public Float getLatitude() {
        return spref.getFloat(Constants.Store.PARKING_LOCATION_LATITUDE, -1);
    }

    public Float getLongitude() {
        return spref.getFloat(Constants.Store.PARKING_LOCATION_LONGITUDE, -1);
    }

    /**
     * Returns a list of Bluetooth devices, which are tracked by the user for auto-parking.
     */
    public Set<String> getDevices() {
        Set<String> s = new HashSet<>(); // default value
        listPrefs();
        return spref.getStringSet(Constants.Store.DEVICE_ADDRESSES, s);
    }

    /**
     * Removes location, IS_PARKED, parking time, parking type.
     */
    public void removeLocation() {
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

    boolean shouldSendNotification() {
        return spref.getBoolean(context.getString(R.string.pref_key_show_notif),
                context.getResources().getBoolean(R.bool.pref_show_notif_default));
    }

    private void listPrefs() {
        Map<String, ?> prefs = PreferenceManager.getDefaultSharedPreferences(
                context).getAll();
        for (String key : prefs.keySet()) {
            Object pref = prefs.get(key);
            Log.e("pref", "Pref value: " + key + ": " + pref);
        }

    }
}
