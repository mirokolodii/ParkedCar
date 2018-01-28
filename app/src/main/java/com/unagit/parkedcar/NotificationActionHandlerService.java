package com.unagit.parkedcar;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import static com.unagit.parkedcar.MainActivity.LOG_TAG;

/**
 * Created by a264889 on 28.01.2018.
 */

public class NotificationActionHandlerService extends IntentService {
    public NotificationActionHandlerService() {
        super("NotificationActionHandlerService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String action = intent.getAction();
        Log.d(LOG_TAG, "We have just entered NotificationActionHandlerService");

        // Show parking location in Google Maps
        if (action.equals(Constants.Notifications.ACTION_SHOW_ON_MAP)) {
            Log.i(LOG_TAG, "Show_on_map action");

            // Create Google Maps query URL
            MyDefaultPreferenceManager myPreferenceManager = new MyDefaultPreferenceManager(getApplicationContext());
            String uri = Constants.GoogleMaps.GOOGLE_MAPS_QUERY_URL;
            uri += myPreferenceManager.getLatitude() + "," + myPreferenceManager.getLongitude();
            Log.i(LOG_TAG, "Google Maps URL: " + uri);
            // Initiate new intent
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
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
                    Toast.makeText(this, "Google Maps application is required for this action", Toast.LENGTH_LONG).show();
                }
            }

        } else if (action.equals(Constants.Notifications.ACTION_DIRECTIONS)) {
            // TODO: Show directions from current location to parking location in Google Maps
            Log.i(LOG_TAG, "Directions action");
            Log.i(LOG_TAG, new MyDefaultPreferenceManager(getApplicationContext()).getDevices().toString());

        }else if (action.equals(Constants.Notifications.ACTION_CLEAR)) {
            // Remove location's lat and lon from SharedPreferences
            dismissNotification();


        } else {
            Log.i(LOG_TAG, "Unhandled action");
            throw new IllegalArgumentException("Unsupported action: " + action);
        }

        collapseNotificationBar();
    }

    public void dismissNotification() {
        // Dismiss notification
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        try {
            mNotificationManager.cancel(Constants.Requests.NOTIFICATION_ID);
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

}
