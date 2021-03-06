package com.unagit.parkedcar.helpers;

import com.unagit.parkedcar.BuildConfig;
import com.unagit.parkedcar.R;

/**
 * Global constants to use throughout the application.
 */
public final class Constants {

    /**
     * IDs for different requests within the app (permission requests, notification IDs etc.)
     */
    public static class Requests {
        // Request to enable bluetooth on a device
        public static final int ENABLE_BLUETOOTH_ACTIVITY_REQUEST_RESULT = 10;
        // Request to allow access to precise location (FINE_LOCATION).
        public static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;
        // Request to check for 'Location enabled' in device' settings
        public static final int ENABLE_LOCATION_REQUEST_RESULT = 2;
    }

    /**
     * Park actions.
     * SET_PARKING_LOCATION: we want to know current location and set it as parking location.
     * CLEAR_PARKING_LOCATION: clear parking.
     * REQUEST_CURRENT_LOCATION: request current location from device,
     * so that it can be set on a map afterwards.
     * SET_CURRENT_LOCATION: set current location on a map.
     */
    public static class ParkActions {
        public static final int SET_PARKING_LOCATION = 0;
        public static final int CLEAR_PARKING_LOCATION= 1;
        public static final int REQUEST_CURRENT_LOCATION = 2;
        public static final int SET_CURRENT_LOCATION = 3;
    }

    /**
     * Constants, used to setup a notification.
     */
    public static class Notifications {
        // Location notification channel
        public static final String CHANNEL_ID = BuildConfig.APPLICATION_ID + ".park_car_notifications";
        public static final String CHANNEL_NAME = "Parking Location";
        public static final String CHANNEL_DESCRIPTION
                = "Show a location of car's parking place";
        public static final int NOTIFICATION_ID = 100;

        public static final String ACTION_SHOW_ON_MAP = "Show";
        public static final String ACTION_DIRECTIONS = "Directions";
        public static final String ACTION_CLEAR = "Clear";
    }

    /**
     * Constants, used in Google Maps.
     */
    public static class GoogleMaps {
        public static final String GOOGLE_MAPS_QUERY_URL = "https://www.google.com/maps/search/?api=1&query=";
        public static final String GOOGLE_MAPS_DIRECTIONS_URL = "https://www.google.com/maps/dir/?api=1&destination=";
        public static final int Parking_icon = R.drawable.ic_parking_icon;
    }

    /**
     * Results, which are sent by MyLocationManager to it's callback.
     */
    public enum LocationStatus {
        LOCATION_DISABLED,
        LOCATION_PERMISSION_NOT_GRANTED,
        LOCATION_RECEIVED,
        LOCATION_NOT_RECEIVED
    }

    /**
     * Constants, used in a broadcast, receiver for which is listening in ParkFragment.
     * Broadcast informs ParkFragment in following cases:
     * - BluetoothReceiver is triggered either to park or clear parking;
     * - User clicks on notification's clear action.
     */
    public static class Bluetooth {
        public static final String BLUETOOTH_RECEIVER_BROADCAST_ACTION = BuildConfig.APPLICATION_ID + ".BluetoothReceiverBroadcast";
        public static final String BLUETOOTH_RECEIVER_BROADCAST_RESULT = BuildConfig.APPLICATION_ID + ".result";
        public static final String EXTRA_CONNECTION_STATE = "Bluetooth.connection.state";
        public static final String EXTRA_PREV_CONNECTION_STATE = "Bluetooth.previous.connection.state";
    }

    /**
     * Constants, identifying key names for DefaultSharedPreferences.
     */
    public static class Store {
        public static final String PARKING_LOCATION_LATITUDE = "parkingLocationLatitude";
        public static final String PARKING_LOCATION_LONGITUDE = "parkingLocationLongitude";
        public static final String DEVICE_ADDRESSES = "deviceAddresses";
        public static final String IS_PARKED = "isParked";
        public static final String PARKED_TIME = "parkedTime";
        public static final String IS_PARKED_AUTOMATICALLY = "isParkedAutomatically";
    }

    /**
     * Padding for 'Park Car' button
     * BIG - button's text = 'Park Car';
     * SMALL - button's text = "Clear'.
     */
    public static class ParkButtonPadding {
        public static final int[] BIG_PORTRAIT = new int[]{60, 30, 70, 30};
        public static final int[] SMALL_PORTRAIT = new int[]{40, 20, 50, 20};
        public static final int[] BIG_LANDSCAPE = new int[]{20, 40, 20, 40};
        public static final int[] SMALL_LANDSCAPE = new int[]{50, 20, 50, 20};
    }
}