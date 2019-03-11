package com.unagit.parkedcar.tools;

import com.google.android.gms.maps.model.LatLng;
import com.unagit.parkedcar.helpers.Constants;
import io.reactivex.Completable;

public class AppLocationProviderImp implements AppLocationProvider {
    private AppPreferenceManager appPreferenceManager;

    public AppLocationProviderImp(AppPreferenceManager appPreferenceManager) {
        this.appPreferenceManager = appPreferenceManager;
    }

    @Override
    public Completable requestLocation(Constants.LocationRequestType type) {
        return Completable.create(emitter -> {
            LatLng location;
            if (type == Constants.LocationRequestType.CURRENT_LOCATION) {
                location = new LatLng(51.1354245, 17.0573938);
                appPreferenceManager.setCurrentLocation(location);
            } else {
                location = new LatLng(51.6468618, 17.7683101);
                appPreferenceManager.setParkingLocation(location);
            }
            emitter.onComplete();
        });
    }
}
