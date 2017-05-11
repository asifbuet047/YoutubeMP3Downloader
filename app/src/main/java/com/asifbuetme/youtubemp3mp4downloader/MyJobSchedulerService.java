package com.asifbuetme.youtubemp3mp4downloader;

import android.app.Notification;
import android.app.NotificationManager;

import com.asifbuetme.youtubemp3mp4downloader.R;

import me.tatarka.support.job.JobParameters;
import me.tatarka.support.job.JobService;

/**
 * Created by ASIF on 3/7/2017.
 */

public class MyJobSchedulerService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (getSharedPreferences("AppPref", MODE_PRIVATE).getInt("times", 0) == 0) {
            Notification notification = new Notification.Builder(getApplicationContext()).setContentTitle("Times of running service").setContentText("No of runs: " + String.valueOf(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("times", 0))).setSmallIcon(R.drawable.ic_favorite_black_24dp).build();
            notificationManager.notify(10, notification);
            getSharedPreferences("AppPref", MODE_PRIVATE).edit().putInt("times", 1).apply();

        } else {
            int i = getSharedPreferences("AppPref", MODE_PRIVATE).getInt("times", 0);
            Notification notification = new Notification.Builder(getApplicationContext()).setContentTitle("Times of running service").setContentText("No of runs: " + String.valueOf(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("times", 0))).setSmallIcon(R.drawable.ic_favorite_black_24dp).build();
            notificationManager.notify(10, notification);
            getSharedPreferences("AppPref", MODE_PRIVATE).edit().putInt("times", i + 1).apply();
        }
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
