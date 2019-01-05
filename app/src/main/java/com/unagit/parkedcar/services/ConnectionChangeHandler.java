package com.unagit.parkedcar.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.unagit.parkedcar.helpers.Helpers;

import java.util.concurrent.TimeUnit;

public class ConnectionChangeHandler extends Service {
    private static int FOREGROUND_NOTIFICATION_ID = 222;
    public ConnectionChangeHandler() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        startForeground(FOREGROUND_NOTIFICATION_ID,
                Helpers.getForegroundNotification(getBaseContext())
        );

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                stopForeground(true);
                stopSelf();
            }
        };

        new Handler().postDelayed(runnable, TimeUnit.SECONDS.toMillis(10));

        return Service.START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't have to implement this method
        return null;
    }

}
