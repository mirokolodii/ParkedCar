package com.unagit.parkedcar.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.unagit.parkedcar.tools.AppPreferenceManager;
import com.unagit.parkedcar.helpers.Constants;
import static com.unagit.parkedcar.views.MainActivity.LOG_TAG;

/**
 * Handles actions from notification.
 */
public class NotificationActionHandlerService extends IntentService {
    public NotificationActionHandlerService() {
        super("NotificationActionHandlerService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String action = intent.getAction();

        // Show parking location in Google Maps app.
        if (action.equals(Constants.Notifications.ACTION_SHOW_ON_MAP)) {
            // Show location on google maps
            showLocationOnMaps();
        }

        // Show directions dialog in Google Maps app.
        else if (action.equals(Constants.Notifications.ACTION_DIRECTIONS)) {
            showDirections();

        }

        // Clear parking location, dismiss notification and inform UI via broadcast.
        else if (action.equals(Constants.Notifications.ACTION_CLEAR)) {
            // Remove location from SharedPreferences
            new AppPreferenceManager(this).removeLocation();
            // Remove notification
            dismissNotification();
            // Send broadcast to inform UI about a need to clear parking.
            sendBroadcast();
        }

        // Unhandled action
        else {
            throw new IllegalArgumentException("Unsupported action: " + action);
        }

        // We want to collapse notification bar, once user clicks one of notification actions.
        collapseNotificationBar();
    }

    /**
     * Opens Google Maps application with parking location marker.
     */
    private void showLocationOnMaps() {
        // Create Google Maps query URL
        AppPreferenceManager myPreferenceManager = new AppPreferenceManager(getApplicationContext());
        String uri = Constants.GoogleMaps.GOOGLE_MAPS_QUERY_URL;
        uri += myPreferenceManager.getLatitude() + "," + myPreferenceManager.getLongitude();
        createMapsIntent(uri);
    }

    /**
     * Opens Google Maps application with direction dialog from current location to parking location.
     */
    private void showDirections() {
        // Build Google Maps query for directions
        AppPreferenceManager myPreferenceManager = new AppPreferenceManager(getApplicationContext());
        String uri = Constants.GoogleMaps.GOOGLE_MAPS_DIRECTIONS_URL;
        uri += myPreferenceManager.getLatitude() + "," + myPreferenceManager.getLongitude();
        Log.d(LOG_TAG, "Directions URI: " + uri);
        createMapsIntent(uri);
    }

    /**
     * Creates intent to open Google Maps app with specified action.
     * @param uri
     */
    private void createMapsIntent(String uri) {
        // Initiate new intent
        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Open intent in Google Maps
        mapsIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapsIntent);
        }
        // If Google Maps app is not installed, try with other apps that can handle this intent
        catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(unrestrictedIntent);
            }
            // No apps are available on a device to handle the intent
            catch (ActivityNotFoundException innerEx) {
                Toast.makeText(this, "Install Google Maps first.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void dismissNotification() {
        // Dismiss notification
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        try {
            mNotificationManager.cancel(Constants.Notifications.NOTIFICATION_ID);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Collapse notification bar
     */
    private void collapseNotificationBar() {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);
    }

    private void sendBroadcast() {
        Intent intent = new Intent(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}