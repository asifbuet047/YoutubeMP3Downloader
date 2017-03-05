package com.example.asif.youtubemp3downloader;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DownloadYoutubeAudioByDownloadManager extends Service {

    String title, youtube_video_id;
    GetResponseFromInternet getResponseFromInternet;
    DownloadManager downloadManager;
    NotificationManager notificationManager;

    public DownloadYoutubeAudioByDownloadManager() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.title = intent.getStringExtra("TITLE");
        this.youtube_video_id = intent.getStringExtra("VIDEOID");
        final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" + title + ".mp3");

        try {
            if (!file.exists()) {
                if (isNetworkAvailable()) {
                    getResponseFromInternet = new GetResponseFromInternet(DownloadYoutubeAudioByDownloadManager.this, "GET", 2000);
                    getResponseFromInternet.setActionName("com.asif.youtubeplaylistvideoactivity.token");
                    getResponseFromInternet.execute("https://www.youtubeinmp3.com/fetch/?format=json&video=http://www.youtube.com/watch?v=" + youtube_video_id);
                    String response = getResponseFromInternet.get();
                    if (getResponseFromInternet.client.getResponseCode() == 200) {
                        JSONObject jsonObject = new JSONObject(response);
                        String conversion_link = jsonObject.getString("link");
                        getResponseFromInternet = new GetResponseFromInternet(DownloadYoutubeAudioByDownloadManager.this, "HEAD", 3000);
                        getResponseFromInternet.execute(conversion_link);
                        getResponseFromInternet.get();
                        String download_link = getResponseFromInternet.client.getHeaderField("Location");

                        if (getResponseFromInternet.client.getResponseCode() == 302) {
                            getResponseFromInternet = new GetResponseFromInternet(DownloadYoutubeAudioByDownloadManager.this, "HEAD", 5000);
                            getResponseFromInternet.execute("https:" + download_link);
                            getResponseFromInternet.get();

                            if (getResponseFromInternet.client.getResponseCode() == 200) {
                                if (getResponseFromInternet.client.getHeaderField("Content-Type").equals("audio/mpeg")) {
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https:" + download_link));
                                    request.allowScanningByMediaScanner();
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                                    request.setAllowedOverRoaming(false);
                                    request.setDescription("File is downloading");
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, title + ".mp3");
                                    request.setTitle(title);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                    downloadManager.enqueue(request);

                                } else {
                                    notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setContentTitle("Something goes wrong").setContentText("File is not mp3 format...Try again later").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_audiotrack).build());
                                }
                            } else {
                                notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setContentTitle("Something goes wrong").setContentText("200 not found...Download cant be start...Try again later").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_audiotrack).build());
                            }

                        } else {
                            notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setContentTitle("Something goes wrong").setContentText("302 not found...Download cant be start...Try again later").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_audiotrack).build());
                        }
                    } else {
                        notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setContentTitle("Something goes wrong").setContentText("Server doesn't response...Try again later").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_audiotrack).build());
                    }
                } else {
                    notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setContentTitle("Something goes wrong").setContentText("Internet isn't available...Check your internet settings").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_audiotrack).build());
                }
            } else {
                notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setContentTitle("Something goes wrong").setContentText(" The file is already exits...No need to download").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_audiotrack).build());
            }
        } catch (InterruptedException | ExecutionException | IOException | JSONException e) {
            Log.e("DownloadYoutubeAudio", e.toString());
        }

        return START_STICKY;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
