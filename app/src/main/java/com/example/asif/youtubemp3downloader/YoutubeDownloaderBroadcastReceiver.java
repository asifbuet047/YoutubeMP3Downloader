package com.example.asif.youtubemp3downloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

public class YoutubeDownloaderBroadcastReceiver extends BroadcastReceiver {

    static String YoutubeDownloaderBroadcastReceiverActionName = "YOUTUBE_MP3_DOWNLOADER_BROADCAST_RECEIVER_INTENT";
    SharedPreferences preferences;

    public YoutubeDownloaderBroadcastReceiver() {


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        /*preferences = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
        int i = preferences.getInt("HOWMANY", 0);
        i++;
        preferences.edit().putInt("HOWMANY", i).apply();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(50002, new Notification.Builder(context).setContentTitle("Times Run").setContentText("No: " + String.valueOf(i) + "  Time Diffs:" + String.valueOf((System.currentTimeMillis() - preferences.getLong("TIME", 0)) / 1000.0) + "s").setAutoCancel(false).setOngoing(true).setSmallIcon(R.drawable.common_google_signin_btn_icon_dark).build());
        preferences.edit().putLong("TIME", System.currentTimeMillis()).apply();

        Intent intent1 = new Intent(context, YoutubeAudioDownloaderService.class);
        context.startService(intent1);*/

        //AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15000, PendingIntent.getBroadcast(context.getApplicationContext(), 520, new Intent(context.getApplicationContext(), YoutubeDownloaderBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
        /*NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123456, new Notification.Builder(context).setContentTitle("Action Name").setContentText(intent.getAction()).setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.common_google_signin_btn_icon_dark).build());
        Toast.makeText(context,intent.getAction(),Toast.LENGTH_LONG).show();*/

    }
}
