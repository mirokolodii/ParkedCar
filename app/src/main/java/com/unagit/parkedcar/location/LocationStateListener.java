package com.unagit.parkedcar.location;

import com.google.android.gms.common.api.ApiException;

public interface LocationStateListener {
    void onLocationEnabled();
    void onLocationDisabled(ApiException exception);
}
