package com.unagit.parkedcar.helpers;

import com.unagit.parkedcar.BuildConfig;
import com.unagit.parkedcar.R;

import java.util.ArrayList;

/**
 * Created by a264889 on 28.01.2018.
 */

public final class Constants {
    public static class Tabs {
        /**
         * Number of tabs to show
         */
        public static final int TABS_COUNT = 3;
        /**
         * Positions of tabs in TabLayout
         */
        public static final int PARK_TAB = 0;
        public static final int PHOTOS_TAB = 1;
        public static final int BLUETOOTH_TAB = 2;
        /**
         * Icons for tabs in TabLayout
         */
        public static final int MAP_TAB_ICON = android.R.drawable.ic_menu_mylocation;
        public static final int PHOTOS_TAB_ICON = android.R.drawable.ic_menu_camera;
        public static final int BLUETOOTH_TAB_ICON = android.R.drawable.stat_sys_data_bluetooth;
    }

    /**
     * IDs for different requests within the app (permission requests, notification IDs etc.)
     */
    public static class Requests {
        // Request to enable bluetooth on a device
        public static final int ENABLE_BLUETOOTH_ACTIVITY_REQUEST = 10;
        public static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;
        // Request to check for 'Location enabled' in device' settings
        public static final int REQUEST_CHECK_SETTINGS = 2;
    }

    public static class ParkActions {
        public static final int SET_PARKING_LOCATION = 0;
        public static final int CLEAR_PARKING_LOCATION= 1;
        public static final int REQUEST_CURRENT_LOCATION = 2;
        public static final int SET_CURRENT_LOCATION = 3;
    }

    public static class Notifications {
        // ID of channel
        public static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".park_car_notifications";
        // User-visible name of the channel
        public static final String NOTIFICATION_CHANNEL_NAME = "Park Car Notifications";
        // User-visible description of the channel
        public static final String NOTIFICATION_CHANNEL_DESCRIPTION
                = "Used to show notifications with information about location of parked car.";
        public static final int NOTIFICATION_ID = 100;
        public static final String NOTIFICATION_TITLE = "Parked Car";
        public static final String NOTIFICATION_TEXT = "Your Car has been marked on the map.";
        public static final String NOTIFICATION_ACTION_TITLE_SHOW = "Show Location";
        public static final String NOTIFICATION_ACTION_TITLE_DIRECTIONS = "Get Directions";
        public static final String NOTIFICATION_ACTION_TITLE_CLEAR = "Clear";
        public static final String ACTION_SHOW_ON_MAP = "Show";
        public static final String ACTION_DIRECTIONS = "Directions";
        public static final String ACTION_CLEAR = "Clear";
    }

    public static class GoogleMaps {
        public static final String GOOGLE_MAPS_QUERY_URL = "https://www.google.com/maps/search/?api=1&query=";
        public static final int Parking_icon = R.drawable.ic_parking_icon;
    }

    public static class Location {
        // Result, returned from MyLocationManager instance via callback
        public static final int LOCATION_DISABLED = 1;
        public static final int LOCATION_PERMISSION_NOT_GRANTED = 2;
        public static final int LOCATION_RECEIVED = 3;
    }

    public static class ParkTypeText {
        public static final String PARKED_AUTOMATICALLY_TEXT = "Parked automatically";
        public static final String PARKED_MANUALLY_TEXT = "Parked manually";
    }

    public static class Bluetooth {
        public static final String BLUETOOTH_RECEIVER_BROADCAST_ACTION = BuildConfig.APPLICATION_ID + ".BluetoothReceiverBroadcast";
        public static final String BLUETOOTH_RECEIVER_BROADCAST_RESULT = BuildConfig.APPLICATION_ID + ".result";
    }

    public static class Store {
        //Keys for SharedPreferences
        public static final String PARKING_LOCATION_LATITUDE = "parkingLocationLatitude";
        public static final String PARKING_LOCATION_LONGITUDE = "parkingLocationLongitude";
        public static final String DEVICE_ADDRESSES = "deviceAddresses";
        public static final String IS_PARKED = "isParked";
        public static final String PARKED_TIME = "parkedTime";
        public static final String IS_PARKED_AUTOMATICALLY = "isParkedAutomatically";
    }

    public static class ParkButtonPadding {
        public static final int[] BIG_PORTRAIT = new int[]{90, 40, 90, 40};
        public static final int[] BIG_LANDSCAPE = new int[]{20, 40, 20, 40};
        public static final int[] SMALL_PORTRAIT = new int[]{50, 20, 50, 20};
        public static final int[] SMALL_LANDSCAPE = new int[]{50, 20, 50, 20};

    }
}