package com.unagit.parkedcar.tools;

import com.google.android.gms.maps.model.LatLng;
import com.unagit.parkedcar.helpers.Constants;
import io.reactivex.Completable;
import io.reactivex.subjects.CompletableSubject;

public class AppLocationProviderImp implements AppLocationProvider {
    private AppPreferenceManager appPreferenceManager;
    CompletableSubject subject = CompletableSubject.create();


    public AppLocationProviderImp(AppPreferenceManager appPreferenceManager) {
        this.appPreferenceManager = appPreferenceManager;
    }

    @Override
    // https://blog.mindorks.com/understanding-rxjava-subject-publish-replay-behavior-and-async-subject-224d663d452f
    //https://medium.com/@nazarivanchuk/types-of-subjects-in-rxjava-96f3a0c068e4
    public Completable requestLocation(Constants.LocationRequestType type) {
        return subject;
//        return Completable.create(emitter -> {
//            LatLng location;
//            if (type == Constants.LocationRequestType.CURRENT_LOCATION) {
//                location = new LatLng(51.1354245, 17.0573938);
//                appPreferenceManager.setCurrentLocation(location);
//            } else {
//                location = new LatLng(51.6468618, 17.7683101);
//                appPreferenceManager.setParkingLocation(location);
//            }
//            emitter.onComplete();
//        });
    }


}
