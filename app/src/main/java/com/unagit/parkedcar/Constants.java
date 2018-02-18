package com.unagit.parkedcar;

/**
 * Created by a264889 on 28.01.2018.
 */

public final class Constants {

    static class Tabs {
        /**
         * Number of tabs to show
         */
        static final int TABS_COUNT = 3;
        /**
         * Positions of tabs in TabLayout
         */
        static final int PARK_TAB = 0;
        static final int PHOTOS_TAB = 1;
        static final int BLUETOOTH_TAB = 2;
        /**
         * Icons for tabs in TabLayout
         */
        static int MAP_TAB_ICON = android.R.drawable.ic_menu_mylocation;
        static int PHOTOS_TAB_ICON = android.R.drawable.ic_menu_camera;
        static int BLUETOOTH_TAB_ICON = android.R.drawable.stat_sys_data_bluetooth;
    }

    /**
     * IDs for different requests within the app (permission requests, notification IDs etc.)
     */
    static class Requests {
        // Request to enable bluetooth on a device
        static final int ENABLE_BLUETOOTH_ACTIVITY_REQUEST = 10;
        static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;
        // Request to check for 'Location enabled' in device' settings
        static final int REQUEST_CHECK_SETTINGS = 2;
    }

    static class ParkActions {
        static final int SET_PARKING_LOCATION = 0;
        static final int CLEAR_PARKING_LOCATION= 1;
        static final int REQUEST_CURRENT_LOCATION = 2;
        static final int SET_CURRENT_LOCATION = 3;
    }

    static class Notifications {
        // ID of channel
        static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".park_car_notifications";
        // User-visible name of the channel
        static final String NOTIFICATION_CHANNEL_NAME = "Park Car Notifications";
        // User-visible description of the channel
        static final String NOTIFICATION_CHANNEL_DESCRIPTION
                = "Used to show notifications with information about location of parked car.";
        static final int NOTIFICATION_ID = 100;
        static final String NOTIFICATION_TITLE = "Parked Car";
        static final String NOTIFICATION_TEXT = "Your Car has been marked on the map.";
        static final String NOTIFICATION_ACTION_TITLE_SHOW = "Show Location";
        static final String NOTIFICATION_ACTION_TITLE_DIRECTIONS = "Get Directions";
        static final String NOTIFICATION_ACTION_TITLE_CLEAR = "Clear";
        static final String ACTION_SHOW_ON_MAP = "Show";
        static final String ACTION_DIRECTIONS = "Directions";
        static final String ACTION_CLEAR = "Clear";
    }

    static class GoogleMaps {
        static final String GOOGLE_MAPS_QUERY_URL = "https://www.google.com/maps/search/?api=1&query=";
        static final int Parking_icon = R.drawable.ic_parking_icon;
    }

    static class Location {
        // Result, returned from MyLocationManager instance via callback
        static final int LOCATION_DISABLED = 1;
        static final int LOCATION_PERMISSION_NOT_GRANTED = 2;
        static final int LOCATION_RECEIVED = 3;
    }

    static class Store {

        //Keys for SharedPreferences
        static final String PARKING_LOCATION_LATITUDE = "parkingLocationLatitude";
        static final String PARKING_LOCATION_LONGITUDE = "parkingLocationLongitude";
        static final String DEVICE_ADDRESSES = "deviceAddresses";
        static final String IS_PARKED = "isParked";
        static final String PARKED_TIME = "parkedTime";

    }
}