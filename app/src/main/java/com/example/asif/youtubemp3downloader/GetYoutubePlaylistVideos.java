package com.example.asif.youtubemp3downloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class GetYoutubePlaylistVideos extends Service {


    public GetYoutubePlaylistVideos() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        Bundle bundle = new Bundle();
        int totalResults;
        PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideos.this, sharedPreferences.getString("Access Token", null), 5000);
        performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + intent.getStringExtra("playlist_id") + "&fields=pageInfo");
        try {
            totalResults = Integer.parseInt(new JSONObject(performYoutubeRestApi.get()).optJSONObject("pageInfo").optString("totalResults"));
            if (totalResults <= 50) {
                performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideos.this, sharedPreferences.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + intent.getStringExtra("playlist_id") + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId"));
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("VIDEOID", videoId);
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubePlaylistVideos.this);
                Intent data = new Intent("com.asif.activity.youtubeplaylist");
                data.putExtra("Data", bundle);
                localBroadcastManager.sendBroadcast(data);
            } else {
                performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideos.this, sharedPreferences.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + intent.getStringExtra("playlist_id") + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))%2CnextPageToken");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                String nextPageToken = parent.optString("nextPageToken");
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId"));
                }
                for (int j = 1; j <= totalResults / 50; j++) {
                    performYoutubeRestApi = new PerformYoutubeRestApi(GetYoutubePlaylistVideos.this, sharedPreferences.getString("Access Token", null), 5000);
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

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubePlaylistVideos.this);
                Intent data = new Intent("com.asif.activity.youtubeplaylist");
                data.putExtra("Data", bundle);
                localBroadcastManager.sendBroadcast(data);
            }
        } catch (Exception e) {
            Toast.makeText(GetYoutubePlaylistVideos.this, "GetYoutubePlaylistVideos:" + "\n" + e.toString(), Toast.LENGTH_LONG).show();
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(GetYoutubePlaylistVideos.this);
            Intent data = new Intent("com.asif.activity.youtubeplaylist");
            data.putExtra("Data", new Bundle());
            data.putExtra("isPlayList", true);
            localBroadcastManager.sendBroadcast(data);
        }
        return START_STICKY;
    }

    private class PerformYoutubeRestApi extends AsyncTask<String, Integer, String> {

        private Context context;
        public HttpsURLConnection client;
        private String authentication_header;
        private int timeout;
        private String response;


        PerformYoutubeRestApi(Context context, String authentication_header, int timeout) {// Any constructor always
            // is called in UI Thread by OS so context passing is worthy
            this.context = context;
            this.authentication_header = authentication_header;
            this.timeout = timeout;
        }

        @Override
        protected void onPreExecute() {// This method is also called in UI Thread so
            // Toast works fine
            super.onPreExecute();
            //Toast.makeText(context, "Staring", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... uri) {// This methos is called by
            // OS in another worker Thread so here context base work creats ANR msg dont use it
            try {
                // Dont use context to communicate with UI thread this cause ANR msg
                // or desire canot solve
                URL url = new URL(uri[0]);
                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("GET");
                client.setRequestProperty("Host", " www.googleapis.com");
                client.setRequestProperty("Content-Length", "0");
                client.setRequestProperty("Authorization", "Bearer " + authentication_header);
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.connect();
                InputStream inputStream = client.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                inputStream.close();
                response = stringBuilder.toString();
                client.disconnect();
                return response;
            } catch (Exception e) {
                return e.toString();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {// This method is also called
            // in UI Thread so Toast works fine
            super.onPostExecute(result);
            //Toast.makeText(context, "Stoping", Toast.LENGTH_SHORT).show();
        }
    }

}

