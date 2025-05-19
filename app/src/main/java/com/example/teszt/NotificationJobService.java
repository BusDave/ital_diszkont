package com.example.teszt;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class NotificationJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new NotificationHandler(getApplicationContext())
                .send("It's time to drink big guy!");
        ;
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
