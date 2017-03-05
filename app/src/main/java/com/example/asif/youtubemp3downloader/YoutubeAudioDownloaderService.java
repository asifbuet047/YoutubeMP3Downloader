package com.example.asif.youtubemp3downloader;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class YoutubeAudioDownloaderService extends Service {

    SharedPreferences sharedPreferences;
    DownloadManager downloadManager;
    NotificationManager notificationManager;
    AlarmManager alarmManager;
    String title;
    int total_download = 0;
    static int DOWNLOAD_MANAGER_NOTIFICATION_ERROR = 5001;

    public YoutubeAudioDownloaderService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        total_download = sharedPreferences.getInt("TotalDownload", 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.notify(130, new Notification.Builder(YoutubeAudioDownloaderService.this).setSmallIcon(R.drawable.ic_media_route_on_09_light).setContentTitle("HaHaHa!!!").setContentText("Android OS try to kill me but i cant be killed easily. Unsatall me if u want to kill me!!").build());
        startService(new Intent(this, YoutubeAudioDownloaderService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isAccessTokenValid()) {
            if (sharedPreferences.getString("UserPlaylist", null) != null) {
                Bundle bundle = getYoutubePlaylistVideosIdWithTitle(sharedPreferences.getString("UserPlaylist", null));
                if (bundle != null) {
                    int total_songs = Integer.valueOf(getYoutubePlaylistSongCount(sharedPreferences.getString("UserPlaylist", null)));
                    ArrayList<String> videoid = bundle.getStringArrayList("VIDEOID");
                    ArrayList<String> titleList = bundle.getStringArrayList("TITLE");
                    assert titleList != null;
                    title = titleList.get(total_songs - 1);
                    assert videoid != null;
                    if (title.length() > 0) {
                        sharedPreferences.edit().putBoolean("isServiceRunning", false).apply();
                        downloadYoutubeAudioByDownloadManager(videoid.get(total_songs - 1));
                    } else {
                        notificationManager.notify(130, new Notification.Builder(YoutubeAudioDownloaderService.this).setSmallIcon(R.drawable.ic_media_route_on_09_light).setContentTitle("Information!").setContentText("Title is null!!!!!!!").build());
                    }

                } else {
                    notificationManager.notify(120, new Notification.Builder(YoutubeAudioDownloaderService.this).setSmallIcon(R.drawable.ic_media_route_on_09_light).setContentTitle("Information!").setContentText("No Bundle").build());
                }
            }
        } else {
            new GetGoogleApiTokenByAuthentication(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Email", null), "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences).execute();
        }


        return START_STICKY;
    }

    private boolean isAccessTokenValid() {
        return (System.currentTimeMillis() - sharedPreferences.getLong("Time of Token", 100)) < 3500000;
    }

    private String getUserYoutubeLikesPlaylistIdId() {
        if (sharedPreferences.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true");
            try {
                JSONObject jsonObject = new JSONObject(performYoutubeRestApi.get());
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                jsonObject = jsonArray.getJSONObject(0).getJSONObject("contentDetails").getJSONObject("relatedPlaylists");
                sharedPreferences.edit().putString("LikesPlaylist", jsonObject.optString("likes")).apply();
                return jsonObject.optString("likes");

            } catch (Exception e) {
                Toast.makeText(YoutubeAudioDownloaderService.this, e.toString(), Toast.LENGTH_SHORT).show();
                return e.toString();
            }
        } else {
            Toast.makeText(YoutubeAudioDownloaderService.this, "NO AccessToken", Toast.LENGTH_SHORT).show();
            return "NO TOKEN";
        }
    }

    private String getUserYoutubeFavouritesPlaylistId() {
        if (sharedPreferences.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true&fields=items%2FcontentDetails");
            try {

                return new JSONObject(performYoutubeRestApi.get()).optJSONArray("items").optJSONObject(0).optJSONObject("contentDetails").optJSONObject("relatedPlaylists").optString("favorites");

            } catch (Exception e) {
                Toast.makeText(YoutubeAudioDownloaderService.this, "getUserYoutubeFavouritesPlaylistId()" + "\n" + e.toString(), Toast.LENGTH_SHORT).show();
                return e.toString();
            }
        } else {
            Toast.makeText(YoutubeAudioDownloaderService.this, "NO AccessToken", Toast.LENGTH_SHORT).show();
            return "NO TOKEN";
        }
    }

    private String getUserYoutubeUploadsPlaylistId() {
        if (sharedPreferences.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true&fields=items%2FcontentDetails");
            try {

                return new JSONObject(performYoutubeRestApi.get()).optJSONArray("items").optJSONObject(0).optJSONObject("contentDetails").optJSONObject("relatedPlaylists").optString("uploads");

            } catch (Exception e) {
                Toast.makeText(YoutubeAudioDownloaderService.this, "getUserYoutubeFavouritesPlaylistId()" + "\n" + e.toString(), Toast.LENGTH_SHORT).show();
                return e.toString();
            }
        } else {
            Toast.makeText(YoutubeAudioDownloaderService.this, "NO AccessToken", Toast.LENGTH_SHORT).show();
            return "NO TOKEN";
        }
    }

    private String getYoutubePlaylistSongCount(String playlist_id) {
        if (sharedPreferences.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Access Token", null), 1500);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=0&playlistId=" + playlist_id);
            try {
                JSONObject jsonObject = new JSONObject(performYoutubeRestApi.get());
                return jsonObject.getJSONObject("pageInfo").getString("totalResults");
            } catch (Exception e) {
                Toast.makeText(YoutubeAudioDownloaderService.this, e.toString(), Toast.LENGTH_LONG).show();
                return e.toString();
            }
        } else {
            Toast.makeText(YoutubeAudioDownloaderService.this, "NO AccessToken or PlayListID", Toast.LENGTH_SHORT).show();
            return "NO TOKEN OR PLAYLIST ID";
        }
    }

    private Bundle getYoutubePlaylistVideosIdWithTitle(String playlist_id) {
        Bundle bundle = new Bundle();
        int totalResults = 0, currrent = 0;
        PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Access Token", null), 5000);
        performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + playlist_id + "&fields=pageInfo");

        try {
            totalResults = Integer.parseInt(new JSONObject(performYoutubeRestApi.get()).optJSONObject("pageInfo").optString("totalResults"));
            if (totalResults <= 50) {
                performYoutubeRestApi = new PerformYoutubeRestApi(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + playlist_id + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId"));
                    currrent++;
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("VIDEOID", videoId);
                return bundle;
            } else {
                performYoutubeRestApi = new PerformYoutubeRestApi(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + playlist_id + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))%2CnextPageToken");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                String nextPageToken = parent.optString("nextPageToken");
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId"));
                    currrent++;
                }
                for (int j = 1; j <= totalResults / 50; j++) {
                    performYoutubeRestApi = new PerformYoutubeRestApi(YoutubeAudioDownloaderService.this, sharedPreferences.getString("Access Token", null), 5000);
                    performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&pageToken=" + nextPageToken + "&playlistId=" + playlist_id + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))%2CnextPageToken");
                    parent = new JSONObject(performYoutubeRestApi.get());
                    nextPageToken = parent.optString("nextPageToken");
                    for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                        title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                        videoId.add(parent.getJSONArray("items").optJSONObject(i).optJSONObject("snippet").optJSONObject("resourceId").optString("videoId"));
                        currrent++;
                    }
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("VIDEOID", videoId);

                return bundle;
            }

        } catch (Exception e) {
            Log.e("YoutubeMp3Downloader", "getUsersAllYoutubePlaylistsWithTitleAndId()" + "===" + e.toString());
        }
        return bundle;
    }

    private void downloadYoutubeAudioByDownloadManager(String youtube_video_id) {
        try {
            if (sharedPreferences.getBoolean("isDownloadCompleted", true)) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" + title + ".mp3");
                if (!file.exists()) {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://www.youtubeinmp3.com/fetch/?video=https://www.youtube.com/watch?v=" + youtube_video_id));
                    request.allowScanningByMediaScanner();
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                    request.setAllowedOverRoaming(false);
                    request.setDescription("File is downlaoding");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, title + ".mp3");
                    request.setTitle(title);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);
                } else {
                    notificationManager.notify(4588, new Notification.Builder(YoutubeAudioDownloaderService.this).setContentTitle("Already here!!").setContentText("File already exits. No need to download").setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused).build());
                }
            } else {
                notificationManager.notify(4566, new Notification.Builder(YoutubeAudioDownloaderService.this).setContentTitle("Downloading!!").setContentText("Download Manager is already busy to download previous audio. Please wait for finish").setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused).build());
            }
        } catch (Exception e) {
            notificationManager.notify(YoutubeAudioDownloaderService.DOWNLOAD_MANAGER_NOTIFICATION_ERROR, new Notification.Builder(YoutubeAudioDownloaderService.this).setContentTitle("Exception!!").setContentText(e.toString()).setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused).build());
            Toast.makeText(YoutubeAudioDownloaderService.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    private class GetGoogleApiTokenByAuthentication extends AsyncTask<String, String, String> {
        String mEmail, mScope;
        Context context;
        SharedPreferences preferences;

        GetGoogleApiTokenByAuthentication(Context context, String email, String google_scope, SharedPreferences sharedPreferences) {
            this.mEmail = email;
            this.mScope = google_scope;
            this.context = context;
            this.preferences = sharedPreferences;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String token) {
            if (token != null) {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                //Storing Access Token using SharedPreferences
                edit.putString("Access Token", token);
                edit.putLong("Time of Token", System.currentTimeMillis());
                edit.apply();
            }
        }

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(
                        context,
                        sharedPreferences.getString("Email", null),
                        mScope);

            } catch (IOException transientEx) {
                // Network or server error, try later
                Log.e("IOException", transientEx.toString());
            } catch (UserRecoverableAuthException e) {
                // Recover (with e.getIntent())
                Log.e("AuthException", e.toString());
            } catch (GoogleAuthException authEx) {
                // The call is not ever expected to succeed
                // assuming you have already verified that
                // Google Play services is installed.
                Log.e("GoogleAuthException", authEx.toString());
            }
            return token;
        }


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
