package com.unagit.parkedcar.services;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

@TargetApi(21)
public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("Test", "JobScheduler started");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}