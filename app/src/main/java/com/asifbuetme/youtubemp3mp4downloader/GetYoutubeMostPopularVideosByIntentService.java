package com.asifbuetme.youtubemp3mp4downloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.asifbuetme.youtubemp3mp4downloader.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by ASIF on 3/7/2017.
 */
public class GetYoutubeMostPopularVideosByIntentService extends IntentService {

    public GetYoutubeMostPopularVideosByIntentService() {
        super("GetYoutubeMostPopularVideosByIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        Bundle bundle = new Bundle();
        try {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubeMostPopularVideosByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/videos?part=snippet%2Cid%2CcontentDetails&chart=mostPopular&regionCode=in&maxResults=15&videoCategoryId=" + intent.getStringExtra("VIDEO_CATAGORY") + "&fields=items(contentDetails%2Fduration%2Cid%2Csnippet(channelTitle%2CpublishedAt%2Ctitle))");
            JSONObject parent = new JSONObject(performYoutubeRestApi.get());
            ArrayList<String> title = new ArrayList<>();
            ArrayList<String> videoId = new ArrayList<>();
            ArrayList<String> publishedAt = new ArrayList<>();
            ArrayList<String> channelTitle = new ArrayList<>();
            ArrayList<String> duration = new ArrayList<>();
            for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                videoId.add(parent.getJSONArray("items").optJSONObject(i).optString("id"));
                publishedAt.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optString("publishedAt"));
                channelTitle.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optString("channelTitle"));
                duration.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("contentDetails").optString("duration"));
            }
            bundle.putStringArrayList("TITLE", title);
            bundle.putStringArrayList("VIDEOID", videoId);
            bundle.putStringArrayList("PUBLISHEDAT", publishedAt);
            bundle.putStringArrayList("CHANNELTITLE", channelTitle);
            bundle.putStringArrayList("DURATION", duration);

            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubeMostPopularVideosByIntentService.this);
            Intent data = new Intent("com.asif.activity.youtubemostpopular");
            data.putExtra("Data", bundle);
            data.putExtra("isError", false);
            localBroadcastManager.sendBroadcast(data);


        } catch (Exception e) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubeMostPopularVideosByIntentService.this);
            Intent data = new Intent("com.asif.activity.youtubemostpopular");
            data.putExtra("Data", new Bundle(0));
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

        void execute(String uri) throws IOException {
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
            } else {
                NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                manager.notify(596, new Notification.Builder(context).setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_black_24dp).setContentTitle("Error!!!").setContentText("Network Operation failed").build());
                response = "";
            }
        }
    }

}
