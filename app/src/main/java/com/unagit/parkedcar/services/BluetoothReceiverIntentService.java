//package com.unagit.parkedcar.services;
//
//import android.app.IntentService;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothProfile;
//import android.content.Intent;
//import android.content.Context;
//import android.location.Location;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//import com.unagit.parkedcar.brain.MyDefaultPreferenceManager;
//import com.unagit.parkedcar.brain.MyLocationManager;
//import com.unagit.parkedcar.brain.MyNotificationManager;
//import com.unagit.parkedcar.helpers.Constants;
//
//import java.util.Set;
//
//import static com.unagit.parkedcar.activities.MainActivity.LOG_TAG;
//
///**
// * An {@link IntentService} subclass for handling asynchronous task requests in
// * a service on a separate handler thread.
// * <p>
// * TODO: Customize class - update intent actions, extra parameters and static
// * helper methods.
// */
//public class BluetoothReceiverIntentService extends JobIntentService implements MyLocationManager.MyLocationManagerCallback {
//
//    private Context context;
//
//    public BluetoothReceiverIntentService() {
////        super("BluetoothReceiverIntentService");
//    }
//
//
//    /**
//     * Starts this service to perform action Baz with the given parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    // TODO: Customize helper method
//    public static void startBluetoothTrigger(Context context, Bundle extras) {
//        Intent intent = new Intent(context, BluetoothReceiverIntentService.class);
//        intent.putExtras(extras);
////        context.startService(intent);
//        enqueueWork(context, BluetoothReceiverIntentService.class, 1, intent);
//    }
//
//    @Override
//    protected void onHandleWork(@NonNull Intent intent) {
//        Log.d(LOG_TAG, "BluetoothReceiverIntentService is triggered...");
//        if (intent != null) {
//            context = getApplicationContext();
//            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            final Integer connectionState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
//            final Integer prevConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
//            handleBluetoothTrigger(connectionState, prevConnectionState, device);
//        }
//    }
//
////    @Override
////    protected void onHandleIntent(Intent intent) {
////        if (intent != null) {
////            context = getApplicationContext();
////            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
////            final Integer connectionState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
////            final Integer prevConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
////            handleBluetoothTrigger(connectionState, prevConnectionState, device);
////        }
////
////    }
//
//    /**
//     * Handle action BluetoothTrigger in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleBluetoothTrigger(Integer connectionState, Integer prevConnectionState,
//            BluetoothDevice device) {
//
//        String deviceAddress = device.getAddress();
//        Log.d(LOG_TAG, "Action verification Passed. Device: " + device.getName());
//
//        // Proceed further only if remote bluetooth device is tracked by user
//        if(isTrackedDevice(deviceAddress)) {
//
//            Log.d(LOG_TAG, "Device is tracked");
////                // List all extras in bundle
////                Bundle bundle = intent.getExtras();
////                for (String key : bundle.keySet()) {
////                    Object value = bundle.get(key);
////                    Log.d(LOG_TAG, String.format("%s: %s (%s)",
////                            key, value, value.getClass().getName()));
////                }
//
//            // Get connection states
//            Log.d(LOG_TAG, String.format("ConnectionState: %d", connectionState));
//            Log.d(LOG_TAG, String.format("Previous ConnectionState: %d", prevConnectionState));
//            if (connectionState == BluetoothAdapter.STATE_DISCONNECTED /* 0 */
//                    && !(prevConnectionState == BluetoothAdapter.STATE_CONNECTING /* 1 */)) { // device has been disconnected, we need to park
//                // Request current location
//                MyLocationManager myLocationManager = new MyLocationManager(null, context, this);
//                //myLocationManager.requestCurrentLocation();
//                Log.d(LOG_TAG, "BluetoothReceiver: disconnected, getting location.");
//
//            } else if (connectionState == BluetoothAdapter.STATE_CONNECTED /* 2 */ ) { // device has been connected, clear prev parking
//                // 1. clear location
//                new MyDefaultPreferenceManager(context).removeLocation();
//                // 2. clear notification
//                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
//                try {
//                    mNotificationManager.cancel(Constants.Notifications.NOTIFICATION_ID);
//                } catch (NullPointerException e) {
//                    Log.e(LOG_TAG, e.getMessage());
//                }
//                // 3. Send broadcast to update ParkFragment UI
//                sendBroadcast(Constants.ParkActions.CLEAR_PARKING_LOCATION);
//
//                Log.d(LOG_TAG, "BluetoothReceiver: connected, removing location.");
//            }
//        }
//
//
//
//
//
//    }
//
//
//
//    // Handle callback with location, received from MyLocationManager
//    @Override
//    public void locationCallback(int result, Location location) {
//        // We need only case, when location is received
//        if (result == Constants.Location.LOCATION_RECEIVED) {
//            // Save location to DefaultPreferences
//            MyDefaultPreferenceManager myDefaultPreferenceManager = new MyDefaultPreferenceManager(this.context);
//            myDefaultPreferenceManager.saveLocation(location);
//            // Inform that car has been parked automatically
//            myDefaultPreferenceManager.setParkedAutomatically(true);
//            // Send notification
//            new MyNotificationManager().sendNotification(this.context, location);
//            // Send broadcast that car has been parked automatically via bluetooth connection
//            sendBroadcast(Constants.ParkActions.SET_PARKING_LOCATION);
//        }
//    }
//
//    private boolean isTrackedDevice(String address) {
//        Set<String> trackedDevices = new MyDefaultPreferenceManager(this.context).getDevices();
//        return trackedDevices.contains(address);
//    }
//
//    private void sendBroadcast(int result) {
//        // Send broadcast to update ParkFragment UI
//        Intent intent = new Intent(Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_ACTION);
//        intent.putExtra(
//                Constants.Bluetooth.BLUETOOTH_RECEIVER_BROADCAST_RESULT,
//                result
//        );
//        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
//    }
//}
