package com.asifbuetme.youtubemp3mp4downloader;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.asifbuetme.youtubemp3mp4downloader.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DownloadYoutubeAudioByDownloadManager extends IntentService {

    String title, youtube_video_id;
    GetDataFromInternet getDataFromInternet;
    DownloadManager downloadManager;
    NotificationManager notificationManager;

    public DownloadYoutubeAudioByDownloadManager() {
        super("DownloadYoutubeAudioByDownloadManager");

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        this.title = intent.getStringExtra("TITLE");
        this.youtube_video_id = intent.getStringExtra("VIDEOID");
        final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" + title + ".mp3");
        try {
            if (!file.exists()) {
                if (isNetworkAvailable()) {
                    getDataFromInternet = new GetDataFromInternet(DownloadYoutubeAudioByDownloadManager.this, "GET", 2000);
                    getDataFromInternet.execute("https://www.youtubeinmp3.com/fetch/?format=json&video=http://www.youtube.com/watch?v=" + youtube_video_id);
                    String response = getDataFromInternet.get();
                    if (getDataFromInternet.client.getResponseCode() == 200) {
                        JSONObject jsonObject = new JSONObject(response);
                        String conversion_link = jsonObject.getString("download_link");
                        getDataFromInternet = new GetDataFromInternet(DownloadYoutubeAudioByDownloadManager.this, "HEAD", 3000);
                        getDataFromInternet.execute(conversion_link);
                        getDataFromInternet.get();
                        String download_link = getDataFromInternet.client.getHeaderField("Location");
                        download_link = "https:" + download_link;

                        if (getDataFromInternet.client.getResponseCode() == 302) {
                            getDataFromInternet = new GetDataFromInternet(DownloadYoutubeAudioByDownloadManager.this, "HEAD", 5000);
                            getDataFromInternet.execute(download_link);
                            getDataFromInternet.get();

                            if (getDataFromInternet.client.getResponseCode() == 200) {
                                if (getDataFromInternet.client.getHeaderField("Content-Type").equals("audio/mpeg")) {
                                    String download_size = getDataFromInternet.client.getHeaderField("Content-Length");
                                    long size = (long) (Long.parseLong(download_size) / 1.0e6);
                                    Notification.Builder builder = new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this);
                                    builder.setDefaults(Notification.DEFAULT_ALL);
                                    builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
                                    builder.setWhen(System.currentTimeMillis());
                                    builder.setAutoCancel(true);
                                    builder.setContentTitle(title);
                                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp));
                                    builder.setStyle(new Notification.BigTextStyle());
                                    builder.setContentText("File-type:MP3 Download size:" + String.valueOf(size) + "MB" + "\n" + "Click me to download the file");
                                    Intent final_download_intent = new Intent(DownloadYoutubeAudioByDownloadManager.this, DownloadYoutubeAudioOrVideoAfterComfirmation.class);
                                    final_download_intent.putExtra("LINK", download_link);
                                    final_download_intent.putExtra("TITLE", title);
                                    final_download_intent.putExtra("ISAUDIO", true);
                                    builder.setContentIntent(PendingIntent.getService(DownloadYoutubeAudioByDownloadManager.this, 302, final_download_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                    notificationManager.notify(10001, builder.build());

                                } else {
                                    notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp)).setContentTitle("Something goes wrong").setContentText("File is not mp3 format...Try again later").setDefaults(Notification.DEFAULT_ALL).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp)).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                                }
                            } else {
                                notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp)).setContentTitle("Something goes wrong").setContentText("200 not found...Download cant be start...Try again later").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                            }

                        } else {
                            notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp)).setContentTitle("Something goes wrong").setContentText("302 not found...Download cant be start...Try again later").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                        }
                    } else {
                        notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp)).setContentTitle("Something goes wrong").setContentText("Server doesn't response...Try again later").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                    }
                } else {
                    notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp)).setContentTitle("Something goes wrong").setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp)).setStyle(new Notification.BigTextStyle()).setContentText("Internet isn't available...Check your internet settings").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                }
            } else {
                notificationManager.notify(10001, new Notification.Builder(DownloadYoutubeAudioByDownloadManager.this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp)).setContentTitle("Something goes wrong").setContentText(" The file is already exits...No need to download").setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
            }
        } catch (IOException | JSONException e) {
            Log.e("DownloadYoutubeAudio", e.toString());
        }


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class GetDataFromInternet {

        private HttpsURLConnection client;
        private String response;
        private Context context;
        private String method;
        private int timeout;


        GetDataFromInternet(Context context, String method, int timeout) {
            this.method = method;
            this.timeout = timeout;
            this.context = context;
        }


        void execute(String uri) {
            try {
                URL url = new URL(uri);
                client = (HttpsURLConnection) url.openConnection();
                if (method.equals("GET")) {
                    client.setRequestMethod(method);
                    //client.setRequestProperty("Host", uri[0].substring(uri[0].indexOf("//"), uri[0].indexOf("/", 9)));
                    client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                    client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                    client.setConnectTimeout(timeout);
                    client.setReadTimeout(timeout);
                    client.connect();

                    InputStream inputStream = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStream.close();
                    client.disconnect();
                    response = stringBuilder.toString();
                } else if (method.equals("HEAD")) {
                    client.setRequestMethod(method);
                    client.setInstanceFollowRedirects(false);
                    client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                    client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                    client.setConnectTimeout(timeout);
                    client.setReadTimeout(timeout);
                    client.connect();

                    InputStream inputStream = client.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStream.close();
                    client.disconnect();
                    response = stringBuilder.toString();
                }


            } catch (Exception e) {
                response = e.toString();
            }
        }

        String get() {
            return response;
        }
    }
}
