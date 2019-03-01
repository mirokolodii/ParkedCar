package com.unagit.parkedcar.services;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import java.util.concurrent.TimeUnit;

@TargetApi(21)
public class TestJobScheduler {
    private final static int JOB_ID = 1;

    public static void scheduleJob(Context context) {
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, MyJobService.class))
                .setOverrideDeadline(TimeUnit.MINUTES.toMillis(15))
                .build();

        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }
}
