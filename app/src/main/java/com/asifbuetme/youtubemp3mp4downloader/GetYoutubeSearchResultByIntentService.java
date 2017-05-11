package com.asifbuetme.youtubemp3mp4downloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.asifbuetme.youtubemp3mp4downloader.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


public class GetYoutubeSearchResultByIntentService extends IntentService {

    public GetYoutubeSearchResultByIntentService() {
        super("GetYoutubeSearchResultByIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        Bundle bundle = new Bundle();
        PerformYoutubeRestApi performYoutubeRestApi;
        try {
            performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubeSearchResultByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=30&order=viewCount&q=" + intent.getStringExtra("query_string") + "&type=video&videoLicense=any&videoType=any&fields=items(id%2FvideoId%2Csnippet%2Ftitle)");
            JSONObject parent = new JSONObject(performYoutubeRestApi.get());
            ArrayList<String> title = new ArrayList<>();
            ArrayList<String> videoId = new ArrayList<>();
            String ids = "";
            for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("id").optString("videoId"));
                ids += parent.getJSONArray("items").optJSONObject(i).optJSONObject("id").optString("videoId");
                ids += "%2C";
            }
            bundle.putStringArrayList("TITLE", title);
            bundle.putStringArrayList("VIDEOID", videoId);

            performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubeSearchResultByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet&id=" + ids + "&fields=items(contentDetails%2Fduration%2Csnippet(channelTitle%2CpublishedAt%2Ctitle))");
            parent = new JSONObject(performYoutubeRestApi.get());
            ArrayList<String> publishedAt = new ArrayList<>();
            ArrayList<String> channelTitle = new ArrayList<>();
            ArrayList<String> duration = new ArrayList<>();
            for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                publishedAt.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optString("publishedAt"));
                channelTitle.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optString("channelTitle"));
                duration.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("contentDetails").optString("duration"));
            }
            bundle.putStringArrayList("PUBLISHEDAT", publishedAt);
            bundle.putStringArrayList("CHANNELTITLE", channelTitle);
            bundle.putStringArrayList("DURATION", duration);

            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubeSearchResultByIntentService.this);
            Intent data = new Intent("com.asif.activity.youtubesearch");
            data.putExtra("Data", bundle);
            data.putExtra("name", "Your Search results");
            data.putExtra("isError", false);
            localBroadcastManager.sendBroadcast(data);


        } catch (Exception e) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubeSearchResultByIntentService.this);
            Intent data = new Intent("com.asif.activity.youtubesearch");
            data.putExtra("Data", new Bundle());
            data.putExtra("name", intent.getStringExtra("name"));
            data.putExtra("isPlayList", false);
            data.putExtra("isError", true);
            localBroadcastManager.sendBroadcast(data);
        }

    }

    private class PerformYoutubeRestApi {
        private Context context;
        HttpsURLConnection client;
        private String authentication_header;
        private int timeout;
        private String response;

        PerformYoutubeRestApi(Context context, String authentication_header, int timeout) {

            this.context = context;
            this.authentication_header = authentication_header;
            this.timeout = timeout;

        }

        String get() {
            return response;
        }

        void execute(String uri) {
            performNetworkOperation(uri);
        }

        String performNetworkOperation(String uri) {// This methos is called by
            try {

                URL url = new URL(uri);
                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("GET");
                client.setRequestProperty("Host", " www.googleapis.com");
                client.setRequestProperty("Content-Length", "0");
                client.setRequestProperty("Authorization", "Bearer " + authentication_header);
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.connect();

                InputStream inputStream;
                StringBuilder stringBuilder;
                if (client.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    inputStream = client.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream));
                    stringBuilder = new StringBuilder();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStream.close();
                    response = stringBuilder.toString();
                    client.disconnect();
                    return response;
                } else {
                    NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                    manager.notify(596, new Notification.Builder(context).setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_black_24dp).setContentTitle("Error!!!").setContentText("Network Operation failed").build());
                    response = "";
                    return response;
                }


            } catch (Exception e) {
                return e.toString();
            }
        }
    }
}
