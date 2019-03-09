package com.unagit.parkedcar.tools;

import com.unagit.parkedcar.helpers.Constants;

import io.reactivex.Completable;

public interface AppLocationProvider {
    Completable requestLocation(Constants.LocationRequestType type);
}
