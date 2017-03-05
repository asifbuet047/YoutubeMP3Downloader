package com.example.asif.youtubemp3downloader;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static int REQUEST_CODE_PICK_ACCOUNT = 102;
    public static int REQUEST_CODE_AUTH = 101;
    TextView textView;
    DownloadManager downloadManager;
    SharedPreferences pref;
    AlarmManager alarmManager;
    NotificationManager notificationManager;
    ProgressDialog progressDialog;
    LocalBroadcastManager localBroadcastManager;
    MainBroadcastReceiver receiver;
    DrawerLayout drawer;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    ListView listView;
    RecyclerView recyclerView;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);


        textView = (TextView) findViewById(R.id.textView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        setSupportActionBar(toolbar);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Snackbar.make(drawerView, "Drawer is slided", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "Drawers  is slided", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //Snackbar.make(drawerView, "Drawer is opened", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "Drawers  is opened", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                //Snackbar.make(drawerView, "Drawer is closed", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "Drawers  is closed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                //Snackbar.make(drawer, "Drawers sate is changed", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "Drawers sate is changed", Toast.LENGTH_SHORT).show();
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        localBroadcastManager = LocalBroadcastManager.getInstance(MainActivity.this);
        receiver = new MainBroadcastReceiver();
        localBroadcastManager.registerReceiver(receiver, new IntentFilter("com.asif.activity.youtubeplaylist"));


        if (pref.getString("Email", null) != null) {
            textView.setText(pref.getString("Email", null) + " is selected\n");
            navigationView.getMenu().findItem(R.id.sign_in).setTitle("Authenticate");
            if (pref.getString("Access Token", null) != null) {
                textView.setText(pref.getString("Email", null) + " is Authenticated");
                if (isAccessTokenValid()) {
                    if (pref.getString("UserPlaylist", null) != null) {
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                textView.setText("Your total videos in the " + pref.getString("UserPlaylistName", null) + " playlist is " + getYoutubePlaylistSongCount(pref.getString("UserPlaylist", null)));
                            }
                        });
                    } else {
                        textView.setText("You dont select any your playlist");
                    }
                } else {
                    textView.setText("AccessToken has been expired.\nClick to Validate button again");
                }
            } else {
                textView.setText("You didn't authenticate your google account with this app");
                GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, pref.getString("Email", null), "oauth2:https://www.googleapis.com/auth/youtube.readonly");
                authentication.execute();
            }
        } else {
            textView.setText("You didn't select any of  your google account");
            navigationView.getMenu().findItem(R.id.sign_in).setTitle("Signing In");
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_AUTH) & (resultCode == RESULT_OK) & (data != null)) {
            Snackbar.make(navigationView, data.getDataString(), Snackbar.LENGTH_SHORT).show();
        } else if ((requestCode == REQUEST_CODE_PICK_ACCOUNT) & ((data != null))) {
            pref.edit().putString("Email", data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)).apply();
            navigationView.getMenu().findItem(R.id.sign_in).setTitle("Authenticate");
            textView.setText(pref.getString("Email", null) + " has been selected");
            Snackbar.make(navigationView, pref.getString("Email", null) + " has been selected", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(navigationView, "You didn't choose any account", Snackbar.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        // Toast.makeText(MainActivity.this, "onResume()---MainActivity", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(MainActivity.this, "onPause()---MainActivity", Toast.LENGTH_SHORT).show();
    }


    private String downloadYoutubeAudio(String youtube_video_id) {
        String result = "";
        String temp, response, line;
        String link = "https://www.youtubeinmp3.com/fetch/?format=JSON&video=http://www.youtube.com/watch?v=";
        StringBuilder stringBuilder = new StringBuilder();
        HttpsURLConnection client;
        Map<String, List<String>> responseHeaderList;
        Iterator<String> stringIterator;
        InputStream inputStream;
        BufferedReader reader;
        JSONObject jsonObject;

        try {

            URL url = new URL(link + youtube_video_id);
            client = (HttpsURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
            client.setRequestProperty("authority", "www.youtubeinmp3.com");
            client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
            client.setConnectTimeout(2000);
            client.setReadTimeout(2000);
            client.connect();
            inputStream = client.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(inputStream));
            line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            client.disconnect();
            response = stringBuilder.toString();
            stringBuilder = new StringBuilder();
            jsonObject = new JSONObject(response);
            link = jsonObject.getString("link");


            Thread.sleep(2000);


            url = new URL(link);
            client = (HttpsURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
            client.setRequestProperty("authority", "www.youtubeinmp3.com");
            client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
            client.setConnectTimeout(20000);
            client.setReadTimeout(20000);
            client.connect();
            responseHeaderList = client.getHeaderFields();//any client operation should be done in child thread
            stringIterator = responseHeaderList.keySet().iterator();
            while (stringIterator.hasNext()) {
                temp = stringIterator.next();
                if (temp == null) {
                    stringBuilder.append(responseHeaderList.get(null).get(0)).append("\n");
                } else {
                    stringBuilder.append(temp).append(":").append(responseHeaderList.get(temp).get(0)).append("\n");
                }
            }
            result = stringBuilder.append("\n\n\n").toString();
            stringBuilder = new StringBuilder();
            inputStream = client.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(inputStream));
            line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            client.disconnect();
            result = result + stringBuilder.toString();

        } catch (Exception e) {
            Log.e("Downlaod Function", e.toString());
        }
        return result;
    }

    private JSONObject download(String youtube_video_id) {
        String temp, response, line;
        String link = "https://www.youtubeinmp3.com/fetch/?format=JSON&video=http://www.youtube.com/watch?v=";
        StringBuilder stringBuilder = new StringBuilder();
        HttpsURLConnection client;
        Map<String, List<String>> responseHeaderList;
        Iterator<String> stringIterator;
        InputStream inputStream;
        BufferedReader reader;
        JSONObject jsonObject, result = new JSONObject();

        try {

            URL url = new URL(link + youtube_video_id);
            client = (HttpsURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
            client.setRequestProperty("authority", "www.youtubeinmp3.com");
            client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
            client.setConnectTimeout(2000);
            client.setReadTimeout(2000);
            client.connect();
            inputStream = client.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(inputStream));
            line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            client.disconnect();
            response = stringBuilder.toString();
            jsonObject = new JSONObject(response);
            link = jsonObject.getString("link");


            Thread.sleep(2000);


            url = new URL(link);
            client = (HttpsURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
            client.setRequestProperty("authority", "www.youtubeinmp3.com");
            client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
            client.setConnectTimeout(20000);
            client.setReadTimeout(20000);
            client.connect();
            responseHeaderList = client.getHeaderFields();//any client operation should be done in child thread
            stringIterator = responseHeaderList.keySet().iterator();
            while (stringIterator.hasNext()) {
                temp = stringIterator.next();
                if (temp == null) {
                    result.put("ResponseCode", responseHeaderList.get(null).get(0));
                } else {
                    result.put(temp, responseHeaderList.get(temp).get(0));
                }
            }
            stringBuilder = new StringBuilder();
            inputStream = client.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(inputStream));
            line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            client.disconnect();
            result.put("RawFileData", stringBuilder.toString());


        } catch (Exception e) {
            Log.e("YouTubeDownloadFunction", e.toString());
        }
        return result;
    }

    private JSONObject downloadYoutubeAudioFile(String youtube_video_id) {
        String temp, line;
        String link = "https://www.youtubeinmp3.com/fetch/?video=https://www.youtube.com/watch?v=";
        StringBuilder stringBuilder;
        HttpsURLConnection client;
        Map<String, List<String>> responseHeaderList;
        Iterator<String> stringIterator;
        InputStream inputStream;
        BufferedReader reader;
        JSONObject result = new JSONObject();

        try {
            URL url = new URL(link + youtube_video_id);
            client = (HttpsURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
            client.setRequestProperty("authority", "www.youtubeinmp3.com");
            client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
            client.setConnectTimeout(20000);
            client.setReadTimeout(20000);
            client.connect();
            responseHeaderList = client.getHeaderFields();//any client operation should be done in child thread
            stringIterator = responseHeaderList.keySet().iterator();
            while (stringIterator.hasNext()) {
                temp = stringIterator.next();
                if (temp == null) {
                    result.put("ResponseCode", responseHeaderList.get(null).get(0));
                } else {
                    result.put(temp, responseHeaderList.get(temp).get(0));
                }
            }
            stringBuilder = new StringBuilder();
            inputStream = client.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(inputStream));
            line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            client.disconnect();
            result.put("RawFileData", stringBuilder.toString());


        } catch (Exception e) {
            Log.e("YouTubeDownloadFunction", e.toString());
        }
        return result;
    }

    private String getUserYoutubeFavouritesPlaylistId() {
        if (pref.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true&fields=items%2FcontentDetails");
            try {

                return new JSONObject(performYoutubeRestApi.get()).optJSONArray("items").optJSONObject(0).optJSONObject("contentDetails").optJSONObject("relatedPlaylists").optString("favorites");

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "getUserYoutubeFavouritesPlaylistId()" + "\n" + e.toString(), Toast.LENGTH_SHORT).show();
                return e.toString();
            }
        } else {
            Toast.makeText(MainActivity.this, "NO AccessToken", Toast.LENGTH_SHORT).show();
            return "NO TOKEN";
        }
    }

    private String getUserYoutubeUploadsPlaylistId() {
        if (pref.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true&fields=items%2FcontentDetails");
            try {

                return new JSONObject(performYoutubeRestApi.get()).optJSONArray("items").optJSONObject(0).optJSONObject("contentDetails").optJSONObject("relatedPlaylists").optString("uploads");

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "getUserYoutubeFavouritesPlaylistId()" + "\n" + e.toString(), Toast.LENGTH_SHORT).show();
                return e.toString();
            }
        } else {
            Toast.makeText(MainActivity.this, "NO AccessToken", Toast.LENGTH_SHORT).show();
            return "NO TOKEN";
        }
    }

    private String getYoutubeVideoTitle(String video_id) {
        if (pref.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 1500);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/videos?part=snippet&id=" + video_id);
            try {
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                JSONArray items = parent.getJSONArray("items");
                JSONObject info = items.getJSONObject(0);
                return info.getJSONObject("snippet").getString("title");
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                return e.toString();
            }
        } else {
            Toast.makeText(MainActivity.this, "NO AccessToken or PlayListID", Toast.LENGTH_SHORT).show();
            return "NO TOKEN OR PLAYLIST ID";
        }
    }

    private Bundle getUserYoutubeLikesPlaylistVideoWithTitileAndId() {
        if ((pref.getString("LikesPlaylist", null) != null) & (pref.getString("Access Token", null) != null)) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=10&playlistId=" + pref.getString("LikesPlaylist", null));

            try {
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                JSONArray items = parent.getJSONArray("items");
                JSONObject snippet;
                Bundle bundle = new Bundle();
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    snippet = items.optJSONObject(i).getJSONObject("snippet");
                    title.add(i, snippet.optString("title"));
                    videoId.add(i, snippet.optJSONObject("resourceId").optString("videoId"));
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("VIDEOID", videoId);
                return bundle;
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                return null;
            }

        } else {
            Toast.makeText(MainActivity.this, "NO AccessToken or PlayListID", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Bundle getYoutubePlaylistVideosIdWithTitile(String playlist_id) {
        Bundle bundle = new Bundle();
        int totalResults = 0, currrent = 0;
        PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 5000);
        performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + playlist_id + "&fields=pageInfo");

        try {
            totalResults = Integer.parseInt(new JSONObject(performYoutubeRestApi.get()).optJSONObject("pageInfo").optString("totalResults"));
            if (totalResults <= 50) {
                performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + playlist_id + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    videoId.add(parent.getJSONArray("items").optJSONObject(i).optString("id"));
                    currrent++;
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("VIDEOID", videoId);
                return bundle;
            } else {
                performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 5000);
                performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + playlist_id + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))%2CnextPageToken");
                JSONObject parent = new JSONObject(performYoutubeRestApi.get());
                String nextPageToken = parent.optString("nextPageToken");
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> videoId = new ArrayList<>();
                for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                    title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                    videoId.add(parent.getJSONArray("items").optJSONObject(i).optString("id"));
                    currrent++;
                }
                for (int j = 1; j <= totalResults / 50; j++) {
                    performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 5000);
                    performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&pageToken=" + nextPageToken + "&playlistId=" + playlist_id + "&fields=items(snippet(resourceId%2FvideoId%2Ctitle))%2CnextPageToken");
                    parent = new JSONObject(performYoutubeRestApi.get());
                    nextPageToken = parent.optString("nextPageToken");
                    for (int i = 0; i < parent.getJSONArray("items").length(); i++) {
                        title.add(parent.getJSONArray("items").optJSONObject(i).getJSONObject("snippet").optString("title"));
                        videoId.add(parent.getJSONArray("items").optJSONObject(i).optString("id"));
                        currrent++;
                    }
                }
                bundle.putStringArrayList("TITLE", title);
                bundle.putStringArrayList("VIDEOID", videoId);
                bundle.putInt("TOTALCOUNT", currrent);
                Toast.makeText(MainActivity.this, "Total results: " + currrent, Toast.LENGTH_LONG).show();
                return bundle;
            }

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "getUsersAllYoutubePlaylistsWithTitleAndId()" + "\n" + e.toString(), Toast.LENGTH_LONG).show();
        }
        return bundle;
    }

    private String getUserYoutubeLikesPlaylistIdId() {
        if (pref.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true");
            try {
                JSONObject jsonObject = new JSONObject(performYoutubeRestApi.get());
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                jsonObject = jsonArray.getJSONObject(0).getJSONObject("contentDetails").getJSONObject("relatedPlaylists");
                pref.edit().putString("LikesPlaylist", jsonObject.optString("likes")).apply();
                return jsonObject.optString("likes");

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                return e.toString();
            }
        } else {
            Toast.makeText(MainActivity.this, "NO AccessToken", Toast.LENGTH_SHORT).show();
            return "NO TOKEN";
        }
    }

    private String getUserYoutubeLikesPlaylistIdSongCount() {
        if ((pref.getString("LikesPlaylist", null) != null) & (pref.getString("Access Token", null) != null)) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 1500);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=0&playlistId=" + pref.getString("LikesPlaylist", null));
            try {
                JSONObject jsonObject = new JSONObject(performYoutubeRestApi.get());
                return jsonObject.getJSONObject("pageInfo").getString("totalResults");
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                return e.toString();
            }
        } else {
            Toast.makeText(MainActivity.this, "NO AccessToken or PlayListID", Toast.LENGTH_SHORT).show();
            return "NO TOKEN OR PLAYLIST ID";
        }
    }

    private String getYoutubePlaylistSongCount(String playlist_id) {
        if (pref.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, pref.getString("Access Token", null), 1500);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=0&playlistId=" + playlist_id);
            try {
                JSONObject jsonObject = new JSONObject(performYoutubeRestApi.get());
                return jsonObject.getJSONObject("pageInfo").getString("totalResults");
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                return e.toString();
            }
        } else {
            Toast.makeText(MainActivity.this, "NO AccessToken or PlayListID", Toast.LENGTH_SHORT).show();
            return "NO TOKEN OR PLAYLIST ID";
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private boolean isAccessTokenValid() {
        return ((System.currentTimeMillis() - pref.getLong("Time of Token", 100))) < 3500000;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Toast.makeText(MainActivity.this, "Menu is created", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.created_playlist) {

            ArrayList<Information> informations = new ArrayList<>();
            MyRecyclerViewAdapter viewAdapter = new MyRecyclerViewAdapter(MainActivity.this, informations);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(viewAdapter);


            Information information = new Information("ASIF", "1110073");
            informations.add(information);
            information = new Information("SHANTO", "110022");
            informations.add(information);
            viewAdapter.notifyDataSetChanged();

            Toast.makeText(MainActivity.this,"DSDAD",Toast.LENGTH_SHORT).show();

        } else if (id == R.id.favourites_playlist) {
            String favourite_playlist_id;
            if (pref.getString("Favourite_Playlist_ID", null) == null) {
                favourite_playlist_id = getUserYoutubeFavouritesPlaylistId();
                pref.edit().putString("Favourite_Playlist_ID", favourite_playlist_id).apply();
                Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideos.class);
                intent.putExtra("playlist_id", favourite_playlist_id);
                startService(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideos.class);
                intent.putExtra("playlist_id", pref.getString("Favourite_Playlist_ID", null));
                startService(intent);
            }

        } else if (id == R.id.liked_playlist) {

        } else if (id == R.id.uploaded_playlist) {

        } else if (id == R.id.settings) {

        } else if (id == R.id.sign_in) {
            if (pref.getString("Email", null) != null) {
                if (pref.getString("Aceess Token", null) != null) {
                    if (!isAccessTokenValid()) {
                        GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, pref.getString("Email", null), "oauth2:https://www.googleapis.com/auth/youtube.readonly");
                        authentication.execute();
                    } else {
                        textView.setText("Already athenticated");
                    }
                } else {
                    GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, pref.getString("Email", null), "oauth2:https://www.googleapis.com/auth/youtube.readonly");
                    authentication.execute();
                }
            } else {
                pickUserAccount();
            }
        } else if (id == R.id.sign_different) {
            pickUserAccount();

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private class GetGoogleApiTokenByAuthentication extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        String mEmail, mScope;
        Context context;

        GetGoogleApiTokenByAuthentication(Context context, String email, String google_scope) {
            this.mEmail = email;
            this.mScope = google_scope;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Authenticating....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String token) {
            pDialog.dismiss();
            if (token != null) {
                SharedPreferences.Editor edit = pref.edit();
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
                        mEmail,
                        mScope);

            } catch (IOException transientEx) {
                // Network or server error, try later
                Log.e("IOException", transientEx.toString());
            } catch (UserRecoverableAuthException e) {
                // Recover (with e.getIntent())
                startActivityForResult(e.getIntent(), REQUEST_CODE_AUTH);
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

    public class DownloadManagerDownloadCompletionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            pref.edit().putBoolean("isDownloadCompleted", true).apply();
        }
    }

    private class MainBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.asif.activity.youtubeplaylist")) {
                if (intent.getBundleExtra("Data").getStringArrayList("TITLE").size() > 0) {
                    Toast.makeText(MainActivity.this, "Total Favourites videos is " + String.valueOf(intent.getBundleExtra("Data").getStringArrayList("TITLE").size()), Toast.LENGTH_LONG).show();

                    ArrayList<Information> informations = new ArrayList<>();
                    Information information = new Information("ASIF", "1110073");
                    informations.add(information);
                    information = new Information("SHANTO", "110022");
                    informations.add(information);

                    MyRecyclerViewAdapter viewAdapter = new MyRecyclerViewAdapter(MainActivity.this, informations);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(viewAdapter);


                    /*Bundle youtubeVideoInfo = intent.getBundleExtra("Data");
                    if (youtubeVideoInfo.getStringArrayList("TITLE") != null) {
                        for (int i = 0; i < youtubeVideoInfo.getStringArrayList("TITLE").size(); i++) {
                            youtubeVideoInfo.getStringArrayList("TITLE").set(i, String.valueOf(i + 1) + "." + youtubeVideoInfo.getStringArrayList("TITLE").get(i));
                        }
                        UsersAdapter stringArrayAdapter = new UsersAdapter(youtubeVideoInfo.getStringArrayList("TITLE"));
                        SwingRightInAnimationAdapter animationAdapter = new SwingRightInAnimationAdapter(stringArrayAdapter);
                        animationAdapter.setAbsListView(listView);
                        listView.setAdapter(stringArrayAdapter);
                        Toast.makeText(MainActivity.this, "Total no of Playlist: " + String.valueOf(youtubeVideoInfo.getStringArrayList("TITLE").size()), Toast.LENGTH_LONG).show();
                    }*/

                } else {
                    Toast.makeText(MainActivity.this, "NOOOOOOOOOOOOOOOOO", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class UsersAdapter extends ArrayAdapter<String> {

        public UsersAdapter(ArrayList<String> names) {
            super(MainActivity.this, 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String text = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.content_list_view, parent, false);
            }
            TextView each = (TextView) convertView.findViewById(R.id.content_list_view_text);
            each.setText(text);
            return convertView;
        }
    }


}
