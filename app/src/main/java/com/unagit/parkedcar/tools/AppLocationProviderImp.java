package com.unagit.parkedcar.tools;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.unagit.parkedcar.helpers.Constants;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

public class AppLocationProviderImp implements AppLocationProvider {
    private AppPreferenceManager appPreferenceManager;

    public AppLocationProviderImp(AppPreferenceManager appPreferenceManager) {
        this.appPreferenceManager = appPreferenceManager;
    }

    @Override
    public Completable requestLocation(Constants.LocationRequestType type) {
        return Completable.create(emitter -> {
            LatLng latLng;
            if (type == Constants.LocationRequestType.CURRENT_LOCATION) {
                latLng = new LatLng(51.1354245, 17.0573938);
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                appPreferenceManager.setCurrentLocation(location);
             } else {
                latLng = new LatLng(51.6468618, 17.7683101);
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                appPreferenceManager.setParkingLocation(location);

            }


//            sleep(emitter);
            emitter.onComplete();
        });
    }

    private void sleep(CompletableEmitter emitter) {
        Handler handler = new Handler();
        handler.postDelayed(emitter::onComplete, 5000);
    }

}
