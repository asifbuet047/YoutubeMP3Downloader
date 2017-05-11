package com.asifbuetme.youtubemp3mp4downloader;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import com.asifbuetme.youtubemp3mp4downloader.R;

import java.io.File;


public class DownloadYoutubeAudioOrVideoAfterComfirmation extends IntentService {


    String title = "";
    String download_link = "";
    boolean isaudio;
    DownloadManager downloadManager;
    SharedPreferences sharedPreferences;

    public DownloadYoutubeAudioOrVideoAfterComfirmation() {
        super("DownloadYoutubeAudioOrVideoAfterComfirmation");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        title = intent.getStringExtra("TITLE");
        download_link = intent.getStringExtra("LINK");
        isaudio = intent.getBooleanExtra("ISAUDIO", false);
        sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);

        try {
            if (isaudio) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(download_link));
                request.allowScanningByMediaScanner();
                if (sharedPreferences.getBoolean("WIFI_ONLY", false)) {
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                } else {
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                }
                request.setAllowedOverRoaming(false);
                request.setDescription("File is downloading");
                request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + sharedPreferences.getString("AUDIO_LOCATION", "") + "/" + title + ".mp3")));
                //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, title + ".mp3");
                request.setTitle(title);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                downloadManager.enqueue(request);
            } else {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(download_link));
                request.allowScanningByMediaScanner();
                if (sharedPreferences.getBoolean("WIFI_ONLY", false)) {
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                } else {
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                }
                request.setAllowedOverRoaming(false);
                request.setDescription("File is downloading");
                request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + sharedPreferences.getString("VIDEO_LOCATION", "") + "/" + title + ".mp4")));
                //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, title + ".mp4");
                request.setTitle(title);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                downloadManager.enqueue(request);
            }
        } catch (Exception e) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(10008, new Notification.Builder(DownloadYoutubeAudioOrVideoAfterComfirmation.this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_black_24dp)).setStyle(new Notification.BigTextStyle()).setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_black_24dp).setContentTitle("Error").setContentText(e.toString()).build());
        }
    }
}

