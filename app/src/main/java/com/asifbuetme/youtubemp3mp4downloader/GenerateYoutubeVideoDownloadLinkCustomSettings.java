package com.asifbuetme.youtubemp3mp4downloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class GenerateYoutubeVideoDownloadLinkCustomSettings extends IntentService {


    HashMap<String, String> stringHashMap;
    JSONObject jsonObject;
    GetDataFromInternet getDataFromInternet;
    NotificationManager notificationManager;
    SharedPreferences sharedPreferences;
    Bitmap bitmap;


    String title = "";
    String serverUrl = "";
    String serverId = "";
    String id_process = "";
    String keyHash = "";
    String response = "";
    String dPageId = "";
    String youtube_video_id = "";
    String status = "";
    String jobpc = "";
    String download_link = "no_valid_download_link_is_generated";
    int notification_no;
    int no_of_attemps;
    boolean isDownloadLinkValid = false;


    public GenerateYoutubeVideoDownloadLinkCustomSettings() {
        super("GenerateYoutubeVideoDownloadLinkCustomSettings");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        title = intent.getStringExtra("TITLE");
        youtube_video_id = intent.getStringExtra("VIDEOID");
        notification_no = intent.getIntExtra("NOTIFICATIONO", 0);
        sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        no_of_attemps = sharedPreferences.getInt("no_of_attemps", 3);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_download_white_24dp);
        try {
            Runtime runtime = Runtime.getRuntime();

            Log.e("DownloadYoutubeAudio", "Max memory:" + String.valueOf(runtime.maxMemory() / 1.0e6) + "MB");
            Log.e("DownloadYoutubeAudio", "Free memory:" + String.valueOf(runtime.freeMemory() / 1.0e6) + "MB");
            Log.e("DownloadYoutubeAudio", "Total memory:" + String.valueOf(runtime.totalMemory() / 1.0e6) + "MB");

            final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + sharedPreferences.getString("VIDEO_LOCATION", "") + "/" + title + ".mp4");
            if (!file.exists()) {
                if (isNetworkAvailable()) {
                    stringHashMap = new HashMap<>();
                    stringHashMap.put("function", "validate");
                    stringHashMap.put("args[dummy]", "1");
                    stringHashMap.put("args[urlEntryUser]", "https://www.youtube.com/watch?v=" + youtube_video_id);
                    stringHashMap.put("args[fromConvert]", "urlconverter");
                    stringHashMap.put("args[requestExt]", "mp4");
                    stringHashMap.put("args[nbRetry]", "0");
                    stringHashMap.put("args[videoResolution]", sharedPreferences.getString("VIDEO_RESOLUTION", "-1"));
                    stringHashMap.put("args[audioBitrate]", "0");
                    stringHashMap.put("args[audioFrequency]", "0");
                    stringHashMap.put("args[channel]", "stereo");
                    stringHashMap.put("args[volume]", "0");
                    stringHashMap.put("args[startFrom]", "-1");
                    stringHashMap.put("args[endTo]", "-1");
                    stringHashMap.put("args[custom_resx]", "-1");
                    stringHashMap.put("args[custom_resy]", "-1");
                    stringHashMap.put("args[advSettings]", "true");
                    stringHashMap.put("args[aspectRatio]", "-1");
                    getDataFromInternet = new GetDataFromInternet(GenerateYoutubeVideoDownloadLinkCustomSettings.this, "POST", stringHashMap, 10000);
                    getDataFromInternet.execute("https://www3.onlinevideoconverter.com/webservice");

                    response = getDataFromInternet.get();
                    jsonObject = new JSONObject(response).optJSONObject("result");
                    serverUrl = jsonObject.optString("serverUrl");
                    serverId = jsonObject.optString("serverId");
                    id_process = jsonObject.optString("id_process");
                    keyHash = jsonObject.optString("keyHash");
                    dPageId = jsonObject.optString("dPageId");
                    status = jsonObject.optString("status");

                    if (Integer.parseInt(dPageId) == 0 & dPageId.length() < 3) {
                        stringHashMap = new HashMap<>();
                        stringHashMap.put("function", "processVideo");
                        stringHashMap.put("args[dummy]", "1");
                        stringHashMap.put("args[urlEntryUser]", "https://www.youtube.com/watch?v=" + youtube_video_id);
                        stringHashMap.put("args[fromConvert]", "urlconverter");
                        stringHashMap.put("args[requestExt]", "mp4");
                        stringHashMap.put("args[serverId]", serverId);
                        stringHashMap.put("args[nbRetry]", "0");
                        stringHashMap.put("args[title]", title);
                        stringHashMap.put("args[keyHash]", keyHash);
                        stringHashMap.put("args[serverUrl]", serverUrl);
                        stringHashMap.put("args[id_process]", id_process);
                        stringHashMap.put("args[videoResolution]", sharedPreferences.getString("VIDEO_RESOLUTION", "-1"));
                        stringHashMap.put("args[audioBitrate]", "0");
                        stringHashMap.put("args[audioFrequency]", "0");
                        stringHashMap.put("args[channel]", "stereo");
                        stringHashMap.put("args[volume]", "0");
                        stringHashMap.put("args[startFrom]", "-1");
                        stringHashMap.put("args[endTo]", "-1");
                        stringHashMap.put("args[custom_resx]", "-1");
                        stringHashMap.put("args[custom_resy]", "-1");
                        stringHashMap.put("args[advSettings]", "true");
                        stringHashMap.put("args[aspectRatio]", "-1");

                        getDataFromInternet = new GetDataFromInternet(GenerateYoutubeVideoDownloadLinkCustomSettings.this, "POST", stringHashMap, 10000);
                        getDataFromInternet.execute("https://www3.onlinevideoconverter.com/webservice");

                        response = getDataFromInternet.get();
                        jsonObject = new JSONObject(response).optJSONObject("result");
                        id_process = jsonObject.optString("id_process");
                        keyHash = jsonObject.optString("keyHash");
                        dPageId = jsonObject.optString("dPageId");
                        jobpc = jsonObject.optString("jobpc");

                        Thread.sleep(5000);

                        if (dPageId.length() < 3) {
                            stringHashMap = new HashMap<>();
                            stringHashMap.put("function", "getDownloadVideoFilename");
                            stringHashMap.put("args[dummy]", "1");
                            stringHashMap.put("args[urlEntryUser]", "https://www.youtube.com/watch?v=" + youtube_video_id);
                            stringHashMap.put("args[fromConvert]", "urlconverter");
                            stringHashMap.put("args[requestExt]", "mp4");
                            stringHashMap.put("args[serverId]", serverId);
                            stringHashMap.put("args[nbRetry]", "0");
                            stringHashMap.put("args[title]", title);
                            stringHashMap.put("args[keyHash]", keyHash);
                            stringHashMap.put("args[serverUrl]", serverUrl);
                            stringHashMap.put("args[id_process]", id_process);
                            stringHashMap.put("args[videoResolution]", sharedPreferences.getString("VIDEO_RESOLUTION", "-1"));
                            stringHashMap.put("args[audioBitrate]", "0");
                            stringHashMap.put("args[audioFrequency]", "0");
                            stringHashMap.put("args[channel]", "stereo");
                            stringHashMap.put("args[volume]", "0");
                            stringHashMap.put("args[startFrom]", "-1");
                            stringHashMap.put("args[endTo]", "-1");
                            stringHashMap.put("args[custom_resx]", "-1");
                            stringHashMap.put("args[custom_resy]", "-1");
                            stringHashMap.put("args[advSettings]", "true");
                            stringHashMap.put("args[aspectRatio]", "-1");

                            getDataFromInternet = new GetDataFromInternet(GenerateYoutubeVideoDownloadLinkCustomSettings.this, "POST", stringHashMap, 15000);
                            getDataFromInternet.execute("https://www3.onlinevideoconverter.com/webservice");

                            response = getDataFromInternet.get();
                            jsonObject = new JSONObject(response).optJSONObject("result");
                            id_process = jsonObject.optString("id_process");
                            keyHash = jsonObject.optString("keyHash");
                            dPageId = jsonObject.optString("dPageId");
                            jobpc = jsonObject.optString("jobpc");

                            if (dPageId.length() > 3) {
                                getDataFromInternet = new GetDataFromInternet(GenerateYoutubeVideoDownloadLinkCustomSettings.this, "GET", 15000);
                                getDataFromInternet.execute("https://www.onlinevideoconverter.com/success?id=" + dPageId);
                                response = getDataFromInternet.get();
                                String scrap_of_html = response.substring(response.indexOf("<a style"), response.indexOf("id=\"downloadq\""));
                                download_link = scrap_of_html.substring(scrap_of_html.indexOf("href=\"") + "href=\"".length());
                                download_link = download_link.trim();
                                download_link = download_link.substring(0, download_link.indexOf('"'));

                                if (download_link.contains("title")) {
                                    download_link = download_link.substring(0, download_link.indexOf("&title"));
                                    getDataFromInternet = new GetDataFromInternet(GenerateYoutubeVideoDownloadLinkCustomSettings.this, "HEAD", 15000);
                                    getDataFromInternet.execute(download_link);
                                    if (getDataFromInternet.client.getHeaderField("Content-Type").equals("video/mp4")) {
                                        String download_size = getDataFromInternet.client.getHeaderField("Content-Length");
                                        long size = (long) (Long.parseLong(download_size) / 1.0e6);
                                        Notification.Builder builder = new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this);
                                        builder.setDefaults(Notification.DEFAULT_ALL);
                                        builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
                                        builder.setWhen(System.currentTimeMillis());
                                        builder.setAutoCancel(true);
                                        builder.setContentTitle(title);
                                        builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.success)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
                                        builder.setContentText("File-type:MP4 Size:" + String.valueOf(size) + "MB Click to download the file");
                                        Intent final_download_intent = new Intent(GenerateYoutubeVideoDownloadLinkCustomSettings.this, DownloadYoutubeAudioOrVideoAfterComfirmation.class);
                                        final_download_intent.putExtra("LINK", download_link);
                                        final_download_intent.putExtra("TITLE", title);
                                        final_download_intent.putExtra("ISAUDIO", false);
                                        builder.setContentIntent(PendingIntent.getService(GenerateYoutubeVideoDownloadLinkCustomSettings.this, 302, final_download_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                        notificationManager.notify(notification_no + 1, builder.build());
                                        isDownloadLinkValid = true;

                                    } else {

                                        Log.e("DownloadYoutubeVideo", "Server Error!!  is not mp4 format...Try again later");
                                    }
                                } else if (download_link.contains("http://s")) {
                                    String scrap_for_file = "";
                                    if (response.indexOf(".mp4\t", 0) > 0) {
                                        scrap_for_file = response.substring(response.indexOf(".mp4\t", 0), response.indexOf("</p>", response.indexOf(".mp4\t", 0)));
                                        scrap_for_file = scrap_for_file.replaceAll("\t", "");
                                        scrap_for_file = scrap_for_file.replace("&nbsp;", "");
                                        scrap_for_file = scrap_for_file.replaceAll(".mp4", "");
                                    }

                                    Notification.Builder builder = new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this);
                                    builder.setDefaults(Notification.DEFAULT_ALL);
                                    builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
                                    builder.setWhen(System.currentTimeMillis());
                                    builder.setAutoCancel(true);
                                    builder.setContentTitle(title);
                                    builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.success)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
                                    if (scrap_for_file.length() > 2) {
                                        builder.setContentText("File-type:MP4 Click to download the file");
                                    } else {
                                        builder.setContentText("File-type:MP4 " + "Size:" + scrap_for_file + " Click to download the file");
                                    }
                                    Intent final_download_intent = new Intent(GenerateYoutubeVideoDownloadLinkCustomSettings.this, DownloadYoutubeAudioOrVideoAfterComfirmation.class);
                                    final_download_intent.putExtra("LINK", download_link);
                                    final_download_intent.putExtra("TITLE", title);
                                    final_download_intent.putExtra("ISAUDIO", false);
                                    builder.setContentIntent(PendingIntent.getService(GenerateYoutubeVideoDownloadLinkCustomSettings.this, 302, final_download_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                    notificationManager.notify(notification_no + 1, builder.build());
                                    isDownloadLinkValid = true;
                                } else {
                                    Log.e("DownloadYoutubeVideo", "Server Error!!  Try again");
                                    notificationManager.notify(10002, new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this).setContentTitle("Server error").setContentText("Try later").setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.failure)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                                }
                            } else {
                                Log.e("DownloadYoutubeVideo", "Server Error!!!  Try again..............");
                                notificationManager.notify(10002, new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this).setContentTitle("Server error!!!").setContentText("Try later").setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.failure)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                            }
                        } else {
                            getDataFromInternet = new GetDataFromInternet(GenerateYoutubeVideoDownloadLinkCustomSettings.this, "GET", 15000);
                            getDataFromInternet.execute("https://www.onlinevideoconverter.com/success?id=" + dPageId);
                            response = getDataFromInternet.get();
                            String scrap_of_html = response.substring(response.indexOf("<a style"), response.indexOf("id=\"downloadq\""));
                            download_link = scrap_of_html.substring(scrap_of_html.indexOf("href=\"") + "href=\"".length());
                            download_link = download_link.trim();
                            download_link = download_link.substring(0, download_link.indexOf('"'));

                            if (download_link.contains("title")) {
                                download_link = download_link.substring(0, download_link.indexOf("&title"));
                                getDataFromInternet = new GetDataFromInternet(GenerateYoutubeVideoDownloadLinkCustomSettings.this, "HEAD", 15000);
                                getDataFromInternet.execute(download_link);
                                if (getDataFromInternet.client.getHeaderField("Content-Type").equals("video/mp4")) {
                                    String download_size = getDataFromInternet.client.getHeaderField("Content-Length");
                                    long size = (long) (Long.parseLong(download_size) / 1.0e6);
                                    Notification.Builder builder = new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this);
                                    builder.setDefaults(Notification.DEFAULT_ALL);
                                    builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
                                    builder.setWhen(System.currentTimeMillis());
                                    builder.setAutoCancel(true);
                                    builder.setContentTitle(title);
                                    builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.success)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
                                    builder.setContentText("File-type:MP4 Size:" + String.valueOf(size) + "MB Click to download the file");
                                    Intent final_download_intent = new Intent(GenerateYoutubeVideoDownloadLinkCustomSettings.this, DownloadYoutubeAudioOrVideoAfterComfirmation.class);
                                    final_download_intent.putExtra("LINK", download_link);
                                    final_download_intent.putExtra("TITLE", title);
                                    final_download_intent.putExtra("ISAUDIO", false);
                                    builder.setContentIntent(PendingIntent.getService(GenerateYoutubeVideoDownloadLinkCustomSettings.this, 302, final_download_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                    notificationManager.notify(notification_no + 1, builder.build());
                                    isDownloadLinkValid = true;
                                } else {

                                    Log.e("DownloadYoutubeVideo", "Server Error!!  is not mp4 format...Try again later");
                                }
                            } else if (download_link.contains("http://s")) {
                                String scrap_for_file = "";
                                if (response.indexOf(".mp4\t", 0) > 0) {
                                    scrap_for_file = response.substring(response.indexOf(".mp4\t", 0), response.indexOf("</p>", response.indexOf(".mp4\t", 0)));
                                    scrap_for_file = scrap_for_file.replaceAll("\t", "");
                                    scrap_for_file = scrap_for_file.replace("&nbsp;", "");
                                    scrap_for_file = scrap_for_file.replaceAll(".mp4", "");
                                }

                                Notification.Builder builder = new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this);
                                builder.setDefaults(Notification.DEFAULT_ALL);
                                builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
                                builder.setWhen(System.currentTimeMillis());
                                builder.setAutoCancel(true);
                                builder.setContentTitle(title);
                                builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.success)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
                                if (scrap_for_file.length() > 2) {
                                    builder.setContentText("File-type:MP4 Click to download the file");
                                } else {
                                    builder.setContentText("File-type:MP4 " + "Size:" + scrap_for_file + " Click to download the file");
                                }
                                Intent final_download_intent = new Intent(GenerateYoutubeVideoDownloadLinkCustomSettings.this, DownloadYoutubeAudioOrVideoAfterComfirmation.class);
                                final_download_intent.putExtra("LINK", download_link);
                                final_download_intent.putExtra("TITLE", title);
                                final_download_intent.putExtra("ISAUDIO", false);
                                builder.setContentIntent(PendingIntent.getService(GenerateYoutubeVideoDownloadLinkCustomSettings.this, 302, final_download_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                notificationManager.notify(notification_no + 1, builder.build());
                                isDownloadLinkValid = true;
                            } else {
                                Log.e("DownloadYoutubeVideo", "Server Error!!  Try again");
                                notificationManager.notify(10002, new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this).setContentTitle("Server error").setContentText("Try later").setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.failure)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                            }
                        }

                    } else {
                        Log.e("DownloadYoutubeVideo", "Server Error!  Try again..............");
                        notificationManager.notify(10002, new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this).setContentTitle("Server error!").setContentText("Try later").setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.failure)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                    }

                } else {
                    isDownloadLinkValid = true;
                    notificationManager.notify(10002, new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this).setContentTitle("No Internet!!").setContentText("Internet isn't available..Check your internet settings").setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.failure)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
                }
            } else {

                isDownloadLinkValid = true;
                notificationManager.notify(10002, new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this).setContentTitle("File Exits!!").setContentText("The file is already exits..No need to download").setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.failure)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
            }

        } catch (Exception e) {
            Runtime runtime = Runtime.getRuntime();
            Log.e("DownloadYoutubeVideo", e.toString());
            Log.e("DownloadYoutubeVideo", "Max memory:" + String.valueOf(runtime.maxMemory() / 1.0e6) + "MB");
            Log.e("DownloadYoutubeVideo", "Free memory:" + String.valueOf(runtime.freeMemory() / 1.0e6) + "MB");
            Log.e("DownloadYoutubeVideo", "Total memory:" + String.valueOf(runtime.totalMemory() / 1.0e6) + "MB");
        }

        if (!isDownloadLinkValid) {
            notificationManager.notify(10002, new Notification.Builder(GenerateYoutubeVideoDownloadLinkCustomSettings.this).setContentTitle("Server is too busy now").setContentText("Try again").setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.failure)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS).setSmallIcon(R.drawable.ic_file_download_white_24dp).build());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    private class GetDataFromInternet {

        private HttpsURLConnection client;
        private String response;
        private Context context;
        private String method;
        private int timeout;
        private HashMap<String, String> post_param;


        GetDataFromInternet(Context context, String method, int timeout) {
            this.method = method;
            this.timeout = timeout;
            this.context = context;
        }

        GetDataFromInternet(Context context, String method, HashMap<String, String> data, int timeout) {

            this.method = method;
            this.timeout = timeout;
            this.post_param = data;
            this.context = context;

        }

        void execute(String uri) throws OutOfMemoryError, IOException {

            URL url = new URL(uri);
            client = (HttpsURLConnection) url.openConnection();
            if (method.equals("GET")) {
                client.setRequestMethod(method);
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
            } else if (method.equals("POST")) {
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.setRequestMethod("POST");
                client.setDoOutput(true);
                client.setDoInput(true);
                client.setRequestProperty("Host", "www3.onlinevideoconverter.com");
                client.setRequestProperty("Content-Length", String.valueOf(getPostDataString(post_param).length()));
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                client.setRequestProperty("Origin", "https://www.onlinevideoconverter.com");
                client.setRequestProperty("Referer", "https://www.onlinevideoconverter.com");
                client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                client.connect();

                OutputStream outputStream = client.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(getPostDataString(post_param));
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = client.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
                response = stringBuilder.toString();
                client.disconnect();

            } else if (method.equals("HEAD")) {
                client.setRequestMethod(method);
                client.setInstanceFollowRedirects(true);
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.connect();

                    /*InputStream inputStream = client.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStream.close();
                    reader.close();
                    client.disconnect();
                    response = stringBuilder.toString();*/
            }

        }


        String get() {
            return response;
        }
    }

}