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

public class GetYoutubePlaylistVideosByIntentService extends IntentService {

    public GetYoutubePlaylistVideosByIntentService() {
        super("GetYoutubePlaylistVideosByIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        Bundle bundle = new Bundle();
        int totalResults;
        try {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideosByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + intent.getStringExtra("playlist_id") + "&fields=pageInfo");
            totalResults = Integer.parseInt(new JSONObject(performYoutubeRestApi.get()).optJSONObject("pageInfo").optString("totalResults"));
            if (totalResults <= 50) {
                performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideosByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + intent.getStringExtra("playlist_id") + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                String ids = "";
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId"));
                    ids += parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId");
                    ids += "%2C";
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("VIDEOID", videoId);

                performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideosByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet&id=" + ids + "&maxResults=50&fields=items(contentDetails%2Fduration%2Csnippet(channelTitle%2CpublishedAt%2Ctitle))");
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

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubePlaylistVideosByIntentService.this);
                Intent data = new Intent("com.asif.activity.youtubeplaylistvideos");
                data.putExtra("Data", bundle);
                data.putExtra("name", intent.getStringExtra("name"));
                data.putExtra("isError", false);
                localBroadcastManager.sendBroadcast(data);

            } else {
                performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideosByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + intent.getStringExtra("playlist_id") + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))%2CnextPageToken");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                String nextPageToken = parent.optString("nextPageToken");
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                ArrayList<String> publishedAt = new ArrayList<>();
                ArrayList<String> channelTitle = new ArrayList<>();
                ArrayList<String> duration = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId"));

                }
                for (int j = 1; j <= totalResults / 50; j++) {
                    performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideosByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
                    performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&pageToken=" + nextPageToken + "&playlistId=" + intent.getStringExtra("playlist_id") + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))%2CnextPageToken");
                    parent = new JSONObject(performYoutubeRestApi.get());
                    nextPageToken = parent.optString("nextPageToken");
                    for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                        title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                        videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId"));
                    }
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("VIDEOID", videoId);

                ArrayList<String> arrays_of_ids = new ArrayList<>();
                int temp_location = 0;
                String temp;
                for (int i = 0; i <= totalResults / 40; i++) {
                    temp = "";
                    for (int j = 1; j < 40; j++, temp_location++) {
                        temp += videoId.get(temp_location) + "%2C";
                        if (temp_location == totalResults - 1) {
                            break;
                        }
                    }
                    arrays_of_ids.add(temp);
                }

                for (int k = 0; k <= totalResults / 40; k++) {
                    performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideosByIntentService.this, sharedPreferences.getString("Access Token", null), 5000);
                    performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet&id=" + arrays_of_ids.get(k) + "&maxResults=50&fields=items(contentDetails%2Fduration%2Csnippet(channelTitle%2CpublishedAt%2Ctitle))");
                    parent = new JSONObject(performYoutubeRestApi.get());
                    for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                        channelTitle.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("channelTitle"));
                        publishedAt.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("publishedAt"));
                        duration.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("contentDetails").optString("duration"));
                    }
                }

                bundle.putStringArrayList("PUBLISHEDAT", publishedAt);
                bundle.putStringArrayList("CHANNELTITLE", channelTitle);
                bundle.putStringArrayList("DURATION", duration);

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubePlaylistVideosByIntentService.this);
                Intent data = new Intent("com.asif.activity.youtubeplaylistvideos");
                data.putExtra("Data", bundle);
                data.putExtra("name", intent.getStringExtra("name"));
                data.putExtra("isError", false);
                localBroadcastManager.sendBroadcast(data);
            }
        } catch (Exception e) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubePlaylistVideosByIntentService.this);
            Intent data = new Intent("com.asif.activity.youtubeplaylistvideos");
            data.putExtra("Data", new Bundle(0));
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
