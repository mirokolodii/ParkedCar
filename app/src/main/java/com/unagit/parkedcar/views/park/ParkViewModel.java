package com.unagit.parkedcar.views.park;

import android.app.Application;
import com.unagit.parkedcar.helpers.Constants;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class ParkViewModel extends AndroidViewModel {
    private Constants.ParkStatus parkStatus;

    public ParkViewModel(@NonNull Application application) {
        super(application);
    }

    void onParkButtonClick() {

    }

}
