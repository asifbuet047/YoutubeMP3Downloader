package com.example.asif.youtubemp3downloader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.nhaarman.listviewanimations.appearance.simple.SwingRightInAnimationAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class YoutubePlaylistActivity extends ListActivity {


    Bundle youtubePlaylist;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    LocalBroadcastManager localBroadcastManager;
    YoutubePlaylistBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        youtubePlaylist = getIntent().getBundleExtra("Data");
        ArrayList<String> list = new ArrayList<>();
        list.add("NO Playlist Found");

        sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        localBroadcastManager = LocalBroadcastManager.getInstance(YoutubePlaylistActivity.this);
        broadcastReceiver = new YoutubePlaylistBroadcastReceiver();
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("com.asif.activity.youtubeplaylist"));


        if (youtubePlaylist.getStringArrayList("TITLE") != null) {
            list = youtubePlaylist.getStringArrayList("TITLE");
            for (int i = 0; i < list.size(); i++) {
                list.set(i, String.valueOf(i + 1) + "." + list.get(i));
            }
            UsersAdapter stringArrayAdapter = new UsersAdapter(list);
            SwingRightInAnimationAdapter animationAdapter = new SwingRightInAnimationAdapter(stringArrayAdapter);
            animationAdapter.setAbsListView(getListView());
            setListAdapter(animationAdapter);

            Toast.makeText(YoutubePlaylistActivity.this, "Total no of Playlist: " + String.valueOf(youtubePlaylist.getStringArrayList("TITLE").size()), Toast.LENGTH_LONG).show();
        } else {
            setListAdapter(new ArrayAdapter<>(this, R.layout.row_layout, R.id.title_view, list));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(YoutubePlaylistActivity.this, "onResume()---YoutubePlaylistActivity", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(YoutubePlaylistActivity.this, "onPause()---YoutubePlaylistActivity", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(YoutubePlaylistActivity.this, "onDestroy()---YoutubePlaylistActivity", Toast.LENGTH_SHORT).show();
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Toast.makeText(YoutubePlaylistActivity.this, "onStop()---YoutubePlaylistActivity", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        new AlertDialog.Builder(YoutubePlaylistActivity.this).setMessage("Playlit title: " + youtubePlaylist.getStringArrayList("TITLE").get(position)).setTitle("Want to see?").setPositiveButton("Show videos", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isAccessTokenValid()) {
                    progressDialog = new ProgressDialog(YoutubePlaylistActivity.this);
                    progressDialog.setMessage("Fetching Videos...");
                    progressDialog.setIndeterminate(false);
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    Intent intent = new Intent(YoutubePlaylistActivity.this, GetYoutubePlaylistVideos.class);
                    intent.putExtra("isPlayList", false);
                    intent.putExtra("playlist_id", youtubePlaylist.getStringArrayList("PLAYLISTID").get(position));
                    startService(intent);
                }
            }
        }).setNeutralButton("Confirm Playlist", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sharedPreferences.edit().putString("UserPlaylist", youtubePlaylist.getStringArrayList("PLAYLISTID").get(position)).commit()) {
                    sharedPreferences.edit().putString("UserPlaylistName", youtubePlaylist.getStringArrayList("TITLE").get(position)).apply();
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(2005, new Notification.Builder(YoutubePlaylistActivity.this).setDefaults(Notification.DEFAULT_ALL).setContentTitle("All done!!!").setContentText("Your playlist is  saved.Now whenever You just put new video in this list this app automitically download thats audio format in Your Music directory...").setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused).build());

                } else {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(2006, new Notification.Builder(YoutubePlaylistActivity.this).setDefaults(Notification.DEFAULT_ALL).setContentTitle("Something wrong!!").setContentText("Your playlist is not saved for some reason. Please try again later...").setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused).build());

                }
            }
        }).setNegativeButton("Dont show!", null).create().show();
    }

    private boolean isAccessTokenValid() {
        return (System.currentTimeMillis() - sharedPreferences.getLong("Time of Token", 100)) < 3500000;
    }

    private Bundle getYoutubePlaylistVideosIdWithTitle(String playlist_id) {
        Bundle bundle = new Bundle();
        int totalResults = 0, currrent = 0;
        PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(YoutubePlaylistActivity.this, sharedPreferences.getString("Access Token", null), 5000);
        performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + playlist_id + "&fields=pageInfo");

        try {
            totalResults = Integer.parseInt(new JSONObject(performYoutubeRestApi.get()).optJSONObject("pageInfo").optString("totalResults"));
            if (totalResults <= 50) {
                performYoutubeRestApi = new PerformYoutubeRestApi(YoutubePlaylistActivity.this, sharedPreferences.getString("Access Token", null), 5000);
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
                performYoutubeRestApi = new PerformYoutubeRestApi(YoutubePlaylistActivity.this, sharedPreferences.getString("Access Token", null), 5000);
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
                    performYoutubeRestApi = new PerformYoutubeRestApi(YoutubePlaylistActivity.this, sharedPreferences.getString("Access Token", null), 5000);
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
            Toast.makeText(YoutubePlaylistActivity.this, "getUsersAllYoutubePlaylistsWithTitleAndId()" + "\n" + e.toString(), Toast.LENGTH_LONG).show();
        }
        return bundle;
    }

    private class GetGoogleApiTokenByAuthentication extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        String mEmail, mScope;
        Context context;
        SharedPreferences preferences;

        GetGoogleApiTokenByAuthentication(Context context, String email, String google_scope, SharedPreferences sharedPreferences) {
            this.mEmail = email;
            this.mScope = google_scope;
            this.context = context;
            this.preferences = sharedPreferences;
            preferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(YoutubePlaylistActivity.this);
            pDialog.setMessage("Authenticating....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String token) {
            pDialog.dismiss();
            if (token != null) {
                SharedPreferences.Editor edit = preferences.edit();
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
                        preferences.getString("Email", null),
                        mScope);

            } catch (IOException transientEx) {
                // Network or server error, try later
                Log.e("IOException", transientEx.toString());
            } catch (UserRecoverableAuthException e) {
                // Recover (with e.getIntent())
                startActivityForResult(e.getIntent(), MainActivity.REQUEST_CODE_AUTH);
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

    public class UsersAdapter extends ArrayAdapter<String> {

        public UsersAdapter(ArrayList<String> names) {
            super(YoutubePlaylistActivity.this, 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String text = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(YoutubePlaylistActivity.this).inflate(R.layout.content_list_view, parent, false);
            }
            TextView each = (TextView) convertView.findViewById(R.id.content_list_view_text);
            each.setText(text);
            return convertView;
        }
    }

    private class YoutubePlaylistBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.asif.activity.youtubeplaylist")) {
                progressDialog.cancel();
                Intent data = new Intent(YoutubePlaylistActivity.this, YoutubePlaylistVideoActivity.class);
                data.putExtra("Data", intent.getBundleExtra("Data"));
                startActivity(data);
            }
        }
    }

}
