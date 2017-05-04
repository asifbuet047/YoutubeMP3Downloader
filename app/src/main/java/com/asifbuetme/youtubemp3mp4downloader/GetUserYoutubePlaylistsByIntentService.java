package com.asifbuetme.youtubemp3mp4downloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

public class GetUserYoutubePlaylistsByIntentService extends IntentService {

    public GetUserYoutubePlaylistsByIntentService() {
        super("GetUserYoutubePlaylistsByIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Handler handler = new Handler() {
        };
        SharedPreferences pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        Bundle bundle = new Bundle();
        int totalResults;
        try {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(GetUserYoutubePlaylistsByIntentService.this, pref.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlists?part=snippet&mine=true&fields=pageInfo");
            totalResults = Integer.parseInt(new JSONObject(performYoutubeRestApi.get()).optJSONObject("pageInfo").optString("totalResults"));
            if (totalResults <= 50) {
                performYoutubeRestApi = new PerformYoutubeRestApi(GetUserYoutubePlaylistsByIntentService.this, pref.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlists?part=snippet&maxResults=50&mine=true&fields=items(id%2Csnippet(channelId%2Ctitle))%2CnextPageToken%2CpageInfo");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> playlistId = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    playlistId.add(parent.getJSONArray("items").optJSONObject(i).optString("id"));
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("PLAYLISTID", playlistId);
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetUserYoutubePlaylistsByIntentService.this);
                Intent data = new Intent("com.asif.main_menu.activity.playlist");
                data.putExtra("Data", bundle);
                data.putExtra("name", intent.getStringExtra("name"));
                data.putExtra("isPlayList", true);
                data.putExtra("isError", false);
                localBroadcastManager.sendBroadcast(data);
            } else {
                performYoutubeRestApi = new PerformYoutubeRestApi(GetUserYoutubePlaylistsByIntentService.this, pref.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlists?part=snippet&maxResults=50&mine=true&fields=items(id%2Csnippet(channelId%2Ctitle))%2CnextPageToken%2CpageInfo");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                String nextPageToken = parent.optString("nextPageToken");
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> playlistId = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    playlistId.add(parent.getJSONArray("items").optJSONObject(i).optString("id"));
                }
                for (int j = 1; j <= totalResults / 50; j++) {
                    performYoutubeRestApi = new PerformYoutubeRestApi(GetUserYoutubePlaylistsByIntentService.this, pref.getString("Access Token", null), 5000);
                    performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlists?part=snippet&maxResults=50&mine=true&" + "pageToken=" + nextPageToken + "&fields=items(id%2Csnippet(channelId%2Ctitle))%2CnextPageToken%2CpageInfo");
                    parent = new JSONObject(performYoutubeRestApi.get());
                    nextPageToken = parent.optString("nextPageToken");
                    for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                        title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                        playlistId.add(parent.getJSONArray("items").optJSONObject(i).optString("id"));
                    }
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("PLAYLISTID", playlistId);
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetUserYoutubePlaylistsByIntentService.this);
                Intent data = new Intent("com.asif.main_menu.activity.playlist");
                data.putExtra("Data", bundle);
                data.putExtra("name", intent.getStringExtra("name"));
                data.putExtra("isPlayList", true);
                data.putExtra("isError", false);
                localBroadcastManager.sendBroadcast(data);
            }

        } catch (Exception e) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Log.e("GetUserPlaylist", e.toString());
            manager.notify(596, new Notification.Builder(GetUserYoutubePlaylistsByIntentService.this).setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.ic_file_download_black_24dp).setContentTitle("Error!!!").setContentText("Network Operation failed").build());
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetUserYoutubePlaylistsByIntentService.this);
            Intent data = new Intent("com.asif.main_menu.activity.playlist");
            data.putExtra("Data", new Bundle(0));
            data.putExtra("name", intent.getStringExtra("name"));
            data.putExtra("isPlayList", true);
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


       /* String performNetworkOperation(String uri) {// This methos is called by


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

        }*/
    }
}
