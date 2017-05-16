package com.asifbuetme.youtubemp3mp4downloader;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

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

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 101;
    public static int REQUEST_CODE_PICK_ACCOUNT = 102;
    public static int REQUEST_CODE_AUTH = 101;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.refresh)
    ImageView refresh;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.naviration_view)
    NavigationView navigationView;
    SearchView youtube_search;
    SharedPreferences sharedPreferences;
    AlarmManager alarmManager;
    NotificationManager notificationManager;
    LocalBroadcastManager localBroadcastManager;
    MainBroadcastReceiver receiver;
    ActionBarDrawerToggle actionBarDrawerToggle;

    RecyclerView recyclerView;
    ArrayList<Information> informations;
    RecyclerTouchListener recyclerTouchListener;
    ObjectAnimator animator;
    LinearLayout linearLayout;
    String name;
    String current_category;
    private boolean isLoadPlaylist;
    public static MainActivity mainActivity;
    private boolean from_ownlist_to_preferlist;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSharedPreferences("AppPref", MODE_PRIVATE).getBoolean("DARK_THEME", true)) {
            setTheme(R.style.MyDarkTheme);
        } else {
            setTheme(R.style.MyLightTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        ButterKnife.bind(this);

        navigationView = (NavigationView) findViewById(R.id.naviration_view);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerTouchListener = new RecyclerTouchListener(this, recyclerView, null);
        linearLayout = (LinearLayout) findViewById(R.id.content_main);
        mainActivity = this;

        animator = ObjectAnimator.ofFloat(refresh, View.ROTATION, 0.0f, 360.0f);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        actionBarDrawerToggle.syncState();
        DrawerArrowDrawable drawerArrowDrawable = actionBarDrawerToggle.getDrawerArrowDrawable();
        drawerArrowDrawable.setSpinEnabled(false);
        drawerArrowDrawable.setDirection(DrawerArrowDrawable.ARROW_DIRECTION_END);
        drawerArrowDrawable.setColor(Color.BLACK);
        actionBarDrawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);
        actionBarDrawerToggle.setDrawerSlideAnimationEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);

        sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);

        if (sharedPreferences.getString("AUDIO_LOCATION", null) == null) {
            sharedPreferences.edit().putString("AUDIO_LOCATION", File.separator + Environment.DIRECTORY_MUSIC).apply();
        }
        if (sharedPreferences.getString("VIDEO_LOCATION", null) == null) {
            sharedPreferences.edit().putString("VIDEO_LOCATION", File.separator + Environment.DIRECTORY_MOVIES).apply();
        }
        if (sharedPreferences.getString("AUDIO_BITRATE", null) == null) {
            sharedPreferences.edit().putString("AUDIO_BITRATE", getResources().getStringArray(R.array.list_preference_bitrates_entries_values)[2]).apply();
        }
        if (sharedPreferences.getString("VIDEO_RESOLUTION", null) == null) {
            sharedPreferences.edit().putString("VIDEO_RESOLUTION", getResources().getStringArray(R.array.list_preference_resolution_values)[4]).apply();
        }
        if (sharedPreferences.getString("VIDEO_RESOLUTION_ITAG", null) == null) {
            sharedPreferences.edit().putString("VIDEO_RESOLUTION_ITAG", getResources().getStringArray(R.array.list_preference_resolution_itags)[3]).apply();
        }
        if (sharedPreferences.getString("VIDEO_CATAGORY", null) == null) {
            sharedPreferences.edit().putString("VIDEO_CATAGORY", getResources().getStringArray(R.array.list_youtube_catagories_id)[2]).apply();
        }
        current_category = sharedPreferences.getString("VIDEO_CATAGORY", "28");

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        localBroadcastManager = LocalBroadcastManager.getInstance(MainActivity.this);
        receiver = new MainBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.asif.activity.youtubeplaylistvideos");
        intentFilter.addAction("com.asif.main_menu.activity.playlist");
        intentFilter.addAction("com.asif.activity.youtubesearch");
        intentFilter.addAction("com.asif.activity.youtubemostpopular");
        intentFilter.addAction("com.asif.activity.tokenintent");
        localBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    public static MainActivity getInstance() {
        return mainActivity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_AUTH) & (resultCode == RESULT_OK) & (data != null)) {
            Snackbar.make(navigationView, "You are good to go", Snackbar.LENGTH_SHORT).show();
        } else if ((requestCode == REQUEST_CODE_PICK_ACCOUNT) & ((data != null))) {
            sharedPreferences.edit().putString("Email", data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)).apply();
            sharedPreferences.edit().putString("Type", data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)).apply();
            navigationView.getMenu().findItem(R.id.sign_in).setTitle("Sign Out");
            Snackbar.make(navigationView, sharedPreferences.getString("Email", null) + " has been selected", Snackbar.LENGTH_SHORT).show();
            GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
            authentication.execute();
        } else {
            Snackbar.make(navigationView, "You didn't choose any account", Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission is granted. Click again to download", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission is Required for saving media files", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.created_playlist) {
            if (isOnline()) {
                if (sharedPreferences.getString("Email", null) != null) {
                    if (isAccessTokenValid()) {
                        Intent intent = new Intent(MainActivity.this, GetUserYoutubePlaylistsByIntentService.class);
                        intent.putExtra("name", "Your Created Playlists");

                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        linearLayout.setGravity(Gravity.CENTER);
                        animator.setDuration(2000);
                        animator.setRepeatCount(ObjectAnimator.INFINITE);
                        animator.setRepeatMode(ObjectAnimator.RESTART);
                        animator.setInterpolator(new AccelerateDecelerateInterpolator());
                        animator.start();
                        startService(intent);
                    } else {
                        GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
                        authentication.execute();
                    }
                } else {
                    textView.setText("You dont select any of Your gmail account \n Please select any one by Sign In option in application drawer");
                }
            } else {
                textView.setText("Check network settings. No internet");
            }


        } else if (id == R.id.favourites_playlist) {
            if (isOnline()) {
                if (sharedPreferences.getString("Email", null) != null) {
                    if (isAccessTokenValid()) {
                        String favourite_playlist_id;
                        if (sharedPreferences.getString("Favourite_Playlist_ID", null) == null) {
                            favourite_playlist_id = getUserYoutubeFavouritesPlaylistId();
                            sharedPreferences.edit().putString("Favourite_Playlist_ID", favourite_playlist_id).apply();
                            Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                            intent.putExtra("playlist_id", favourite_playlist_id);
                            intent.putExtra("name", "Your Favourites Playlist");

                            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            linearLayout.setGravity(Gravity.CENTER);
                            animator.setDuration(2000);
                            animator.setRepeatCount(ObjectAnimator.INFINITE);
                            animator.setRepeatMode(ObjectAnimator.RESTART);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.start();

                            startService(intent);
                        } else {
                            Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                            intent.putExtra("playlist_id", sharedPreferences.getString("Favourite_Playlist_ID", null));
                            intent.putExtra("name", "Your Favourites Playlist");

                            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            linearLayout.setGravity(Gravity.CENTER);
                            animator.setDuration(2000);
                            animator.setRepeatCount(ObjectAnimator.INFINITE);
                            animator.setRepeatMode(ObjectAnimator.RESTART);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.start();

                            startService(intent);
                        }
                    } else {
                        GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
                        authentication.execute();
                    }
                } else {
                    textView.setText("You dont select any of Your gmail account \n Please select any one by Sign In option in application drawer");
                }
            } else {
                textView.setText("Check network settings. No internet");
            }

        } else if (id == R.id.liked_playlist) {
            if (isOnline()) {
                if (sharedPreferences.getString("Email", null) != null) {
                    if (isAccessTokenValid()) {
                        String like_playlist_id;
                        if (sharedPreferences.getString("Like_Playlist_ID", null) == null) {
                            like_playlist_id = getUserYoutubeLikesPlaylistId();
                            sharedPreferences.edit().putString("Like_Playlist_ID", like_playlist_id).apply();
                            Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                            intent.putExtra("playlist_id", like_playlist_id);
                            intent.putExtra("name", "Your Liked Playlists");

                            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            linearLayout.setGravity(Gravity.CENTER);
                            animator.setDuration(2000);
                            animator.setRepeatCount(ObjectAnimator.INFINITE);
                            animator.setRepeatMode(ObjectAnimator.RESTART);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.start();

                            startService(intent);
                        } else {
                            Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                            intent.putExtra("playlist_id", sharedPreferences.getString("Like_Playlist_ID", null));
                            intent.putExtra("name", "Your Liked Playlists");

                            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            linearLayout.setGravity(Gravity.CENTER);
                            animator.setDuration(2000);
                            animator.setRepeatCount(ObjectAnimator.INFINITE);
                            animator.setRepeatMode(ObjectAnimator.RESTART);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.start();

                            startService(intent);
                        }
                    } else {
                        GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
                        authentication.execute();
                    }
                } else {
                    textView.setText("You dont select any of Your gmail account \n Please select any one by Sign In option in application drawer");

                }
            } else {
                textView.setText("Check network settings. No internet");
            }


        } else if (id == R.id.uploaded_playlist) {
            if (isOnline()) {
                if (sharedPreferences.getString("Email", null) != null) {
                    if (isAccessTokenValid()) {
                        String upload_playlist_id;
                        if (sharedPreferences.getString("Upload_Playlist_ID", null) == null) {
                            upload_playlist_id = getUserYoutubeUploadsPlaylistId();
                            sharedPreferences.edit().putString("Upload_Playlist_ID", upload_playlist_id).apply();
                            Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                            intent.putExtra("playlist_id", upload_playlist_id);
                            intent.putExtra("name", "Your Uploaded Playlists");

                            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            linearLayout.setGravity(Gravity.CENTER);
                            animator.setDuration(2000);
                            animator.setRepeatCount(ObjectAnimator.INFINITE);
                            animator.setRepeatMode(ObjectAnimator.RESTART);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.start();

                            startService(intent);
                        } else {
                            Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                            intent.putExtra("playlist_id", sharedPreferences.getString("Upload_Playlist_ID", null));
                            intent.putExtra("name", "Your Uploaded Playlists");
                            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            linearLayout.setGravity(Gravity.CENTER);
                            animator.setDuration(2000);
                            animator.setRepeatCount(ObjectAnimator.INFINITE);
                            animator.setRepeatMode(ObjectAnimator.RESTART);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.start();

                            startService(intent);
                        }
                    } else {
                        GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
                        authentication.execute();
                    }
                } else {
                    textView.setText("You dont select any of Your gmail account \n Please select any one by Sign In option in application drawer");
                }
            } else {
                textView.setText("Check network settings. No internet");
            }

        } else if (id == R.id.settings) {

            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

        } else if (id == R.id.sign_in) {
            if (isOnline()) {
                if (sharedPreferences.getString("Email", null) != null) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("Signing out!!!").setMessage("Do You really want to sign out from Your current Google account in this application?").setPositiveButton("Yes, I want", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPreferences.edit().putBoolean("FIRST_TIME", false).clear().apply();
                            textView.setText("You didn't select any of  your google account. Please Sign In");
                            navigationView.getMenu().findItem(R.id.sign_in).setTitle("Sign In");
                            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
                            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0));
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        }
                    }).setNegativeButton("No,Later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "You can sign out at any time You want", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
                } else {
                    pickUserAccount();
                }
            } else {
                textView.setText("Check network settings. No internet");
            }
        } else if (id == R.id.about) {
            new AlertDialog.Builder(MainActivity.this).setTitle("About this Android application").setMessage("Developer: Sheikh Asif\nEmail: asifbuet047@gmail.com").setPositiveButton("Send feedback", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setType("plain/text");
                    sendIntent.setData(Uri.parse("asifbuet047@gmail.com"));
                    sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Your Android application (Youtube MP3 Downloader)");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello. this is a feedback message for your android youtube downloader application. I want to....");
                    startActivity(sendIntent);
                }
            }).setNegativeButton("Back", null).setView(R.layout.nav_header_main).show();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        current_category = sharedPreferences.getString("VIDEO_CATAGORY", "28");
        from_ownlist_to_preferlist = !sharedPreferences.getBoolean("PREFER_LIST", true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MainActivity", "onResume()");
        isLoadPlaylist = false;
        if (sharedPreferences.getBoolean("DARK_THEME", true)) {
            navigationView.setBackgroundColor(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("NAVIGATION_COLOR", ContextCompat.getColor(MainActivity.this, R.color.cardview_dark_background)));
            navigationView.getHeaderView(0).setBackgroundColor(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("TOOLBAR_COLOR", ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray)));
            TextView header = (TextView) navigationView.getHeaderView(0).findViewById(R.id.header_title);
            header.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.materialcolorpicker__black));
            toolbar.setBackgroundColor(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("TOOLBAR_COLOR", ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray)));
            recyclerView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.materialcolorpicker__black));
        } else {
            navigationView.setBackgroundColor(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("NAVIGATION_COLOR", ContextCompat.getColor(MainActivity.this, R.color.materialcolorpicker__dribble)));
            navigationView.getHeaderView(0).setBackgroundColor(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("TOOLBAR_COLOR", ContextCompat.getColor(MainActivity.this, R.color.materialcolorpicker__red)));
            toolbar.setBackgroundColor(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("TOOLBAR_COLOR", ContextCompat.getColor(MainActivity.this, R.color.materialcolorpicker__red)));
            recyclerView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.materialcolorpicker__grey));
        }
        if (sharedPreferences.getString("Email", null) != null) {
            if (sharedPreferences.getString("Access Token", null) != null) {
                if (isAccessTokenValid()) {
                    if (sharedPreferences.getBoolean("PREFER_LIST", true)) {
                        if (!current_category.equals(sharedPreferences.getString("VIDEO_CATAGORY", "28"))) {
                            navigationView.getMenu().findItem(R.id.sign_in).setTitle("Sign Out");
                            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                            linearLayout.setGravity(Gravity.CENTER);
                            animator.setDuration(2000);
                            animator.setRepeatCount(ObjectAnimator.INFINITE);
                            animator.setRepeatMode(ObjectAnimator.RESTART);
                            animator.setInterpolator(new AccelerateInterpolator());
                            animator.start();
                            Intent most_popular = new Intent(MainActivity.this, GetYoutubeMostPopularVideosByIntentService.class);
                            most_popular.putExtra("VIDEO_CATAGORY", sharedPreferences.getString("VIDEO_CATAGORY", "28"));
                            startService(most_popular);
                        } else {
                            if ((informations == null) | (from_ownlist_to_preferlist)) {
                                navigationView.getMenu().findItem(R.id.sign_in).setTitle("Sign Out");
                                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                                refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                                linearLayout.setGravity(Gravity.CENTER);
                                animator.setDuration(2000);
                                animator.setRepeatCount(ObjectAnimator.INFINITE);
                                animator.setRepeatMode(ObjectAnimator.RESTART);
                                animator.setInterpolator(new AccelerateInterpolator());
                                animator.start();
                                Intent most_popular = new Intent(MainActivity.this, GetYoutubeMostPopularVideosByIntentService.class);
                                most_popular.putExtra("VIDEO_CATAGORY", sharedPreferences.getString("VIDEO_CATAGORY", "28"));
                                startService(most_popular);
                            }
                        }
                    } else {
                        navigationView.getMenu().findItem(R.id.sign_in).setTitle("Sign Out");
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        linearLayout.setGravity(Gravity.CENTER);
                        animator.setDuration(2000);
                        animator.setRepeatCount(ObjectAnimator.INFINITE);
                        animator.setRepeatMode(ObjectAnimator.RESTART);
                        animator.setInterpolator(new AccelerateInterpolator());
                        animator.start();
                        Intent intent = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                        intent.putExtra("playlist_id", sharedPreferences.getString("UserPlaylistId", null));
                        intent.putExtra("name", sharedPreferences.getString("UserPlaylistName", null));
                        startService(intent);
                    }
                } else {
                    GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
                    authentication.execute();
                    navigationView.getMenu().findItem(R.id.sign_in).setTitle("Sign Out");
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    linearLayout.setGravity(Gravity.CENTER);
                    animator.setDuration(2000);
                    animator.setRepeatCount(ObjectAnimator.INFINITE);
                    animator.setRepeatMode(ObjectAnimator.RESTART);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.start();
                }
            } else {
                GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
                authentication.execute();
                animator.setDuration(2000);
                animator.setRepeatCount(ObjectAnimator.INFINITE);
                animator.setRepeatMode(ObjectAnimator.RESTART);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.start();
            }
        } else {
            textView.setText("You didn't select any of  your google account. Please Sign In");
            navigationView.getMenu().findItem(R.id.sign_in).setTitle("Sign In");
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
            refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0));
            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isLoadPlaylist) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Close app?").setMessage("Do You want to close this app?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("No", null).show();
            } else {
                if (isAccessTokenValid()) {
                    isLoadPlaylist = false;
                    Intent intent = new Intent(MainActivity.this, GetUserYoutubePlaylistsByIntentService.class);
                    intent.putExtra("name", "Your Created Playlists");

                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    linearLayout.setGravity(Gravity.CENTER);
                    animator.setDuration(2000);
                    animator.setRepeatCount(ObjectAnimator.INFINITE);
                    animator.setRepeatMode(ObjectAnimator.RESTART);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.start();
                    startService(intent);
                } else {
                    GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(MainActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
                    authentication.execute();
                }
            }
        }
        return true;
    }

    private String getUserYoutubeFavouritesPlaylistId() {
        if (sharedPreferences.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true");
            try {
                JSONObject jsonObject = new JSONObject(performYoutubeRestApi.get());
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                jsonObject = jsonArray.getJSONObject(0).getJSONObject("contentDetails").getJSONObject("relatedPlaylists");
                return jsonObject.optString("favorites");
            } catch (Exception e) {
                Log.e("YoutubeMP3Downloader", "getUserYoutubeFavouritesPlaylistId()---" + e.toString());
                return e.toString();
            }
        } else {
            Log.e("YoutubeMP3Downloader", "getUserYoutubeFavouritesPlaylistId()---No Accesstoken");
            return "NO TOKEN";
        }
    }

    private String getUserYoutubeUploadsPlaylistId() {
        if (sharedPreferences.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true");
            try {
                JSONObject jsonObject = new JSONObject(performYoutubeRestApi.get());
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                jsonObject = jsonArray.getJSONObject(0).getJSONObject("contentDetails").getJSONObject("relatedPlaylists");
                return jsonObject.optString("uploads");

            } catch (Exception e) {
                Log.e("YoutubeMP3Downloader", "getUserYoutubeUploadsPlaylistId()---" + e.toString());
                return e.toString();
            }
        } else {
            Log.e("YoutubeMP3Downloader", "getUserYoutubeUploadsPlaylistId()---No Accesstoken");
            return "NO TOKEN";
        }
    }

    private String getUserYoutubeLikesPlaylistId() {
        if (sharedPreferences.getString("Access Token", null) != null) {
            PerformYoutubeRestApi performYoutubeRestApi = new PerformYoutubeRestApi(MainActivity.this, sharedPreferences.getString("Access Token", null), 5000);
            performYoutubeRestApi.execute("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true");
            try {
                JSONObject jsonObject = new JSONObject(performYoutubeRestApi.get());
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                jsonObject = jsonArray.getJSONObject(0).getJSONObject("contentDetails").getJSONObject("relatedPlaylists");
                return jsonObject.optString("likes");

            } catch (Exception e) {
                Log.e("YoutubeMP3Downloader", "getUserYoutubeLikesPlaylistId()---" + e.toString());
                return e.toString();
            }
        } else {
            Log.e("YoutubeMP3Downloader", "getUserYoutubeUploadsPlaylistId()---No Accesstoken");
            return "NO TOKEN";
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private boolean isAccessTokenValid() {
        return ((System.currentTimeMillis() - sharedPreferences.getLong("Time of Token", 100))) < 3500000;
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem search = menu.findItem(R.id.app_bar_search);
        youtube_search = (SearchView) search.getActionView();
        youtube_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (sharedPreferences.getString("Email", null) != null) {
                    Intent search_intent = new Intent(MainActivity.this, GetYoutubeSearchResultByIntentService.class);
                    search_intent.putExtra("query_string", query.replaceAll(" ", "+"));
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    refresh.setLayoutParams(layoutParams);
                    animator.setDuration(2000);
                    animator.setRepeatCount(ObjectAnimator.INFINITE);
                    animator.setRepeatMode(ObjectAnimator.RESTART);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.start();
                    startService(search_intent);
                } else {
                    Toast.makeText(MainActivity.this, "Please sign in", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private int getNotificationNo(String id) {
        int unique = 0;
        char each_chac[] = id.toCharArray();
        for (int i = 0; i < each_chac.length; i++) {
            unique += (int) each_chac[i];
        }
        return unique;
    }

    private void watchYoutubeVideo(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            Log.e("MainActivity", ex.toString());
            startActivity(webIntent);
        }
    }


    private class GetGoogleApiTokenByAuthentication extends AsyncTask<String, String, String> {

        String mScope;
        Context context;
        SharedPreferences preferences;

        GetGoogleApiTokenByAuthentication(Context context, String google_scope, SharedPreferences sharedPreferences) {
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
                edit.putString("Access Token", token);
                edit.putLong("Time of Token", System.currentTimeMillis());
                edit.apply();
                if (sharedPreferences.getBoolean("FIRST_TIME", true)) {
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    linearLayout.setGravity(Gravity.CENTER);
                    animator.setDuration(2000);
                    animator.setRepeatCount(ObjectAnimator.INFINITE);
                    animator.setRepeatMode(ObjectAnimator.RESTART);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.start();
                    Intent most_popular = new Intent(MainActivity.this, GetYoutubeMostPopularVideosByIntentService.class);
                    most_popular.putExtra("VIDEO_CATAGORY", sharedPreferences.getString("VIDEO_CATAGORY", "28"));
                    startService(most_popular);
                    sharedPreferences.edit().putBoolean("FIRST_TIME", false).apply();
                } else {
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MainActivity.this);
                    Intent token_intent = new Intent("com.asif.activity.tokenintent");
                    token_intent.getBooleanExtra("TOKEN", true);
                    localBroadcastManager.sendBroadcast(token_intent);
                }
            } else {
                Log.e("Token", "Token is null");
            }
        }

        @Override
        protected String doInBackground(String... arg0) {
            String token = null;
            try {
                Account account = new Account(sharedPreferences.getString("Email", ""), sharedPreferences.getString("Type", ""));
                token = GoogleAuthUtil.getTokenWithNotification(
                        context,
                        account,
                        mScope, null
                );
                Snackbar.make(drawer, "Authentication is completed", Snackbar.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("IOException", e.toString());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQUEST_CODE_AUTH);
                Log.e("AuthException", e.toString());
            } catch (Exception e) {
                Log.e("GoogleAuthException", e.toString());
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

    private class MainBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals("com.asif.activity.youtubeplaylistvideos")) {
                if (!intent.getBooleanExtra("isError", true)) {
                    if (intent.getBundleExtra("Data").getStringArrayList("TITLE").size() > 0) {
                        Toast.makeText(MainActivity.this, "Total videos in this playlist is " + String.valueOf(intent.getBundleExtra("Data").getStringArrayList("TITLE").size()), Toast.LENGTH_SHORT).show();
                        animator.end();
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                        textView.setText(intent.getStringExtra("name"));
                        name = intent.getStringExtra("name");
                        informations = new ArrayList<>();
                        int info_size = intent.getBundleExtra("Data").getStringArrayList("CHANNELTITLE").size();
                        for (int i = 0; i < info_size; i++) {
                            Information information = new Information();
                            information.setTitle(intent.getBundleExtra("Data").getStringArrayList("TITLE").get(i));
                            information.setId(intent.getBundleExtra("Data").getStringArrayList("VIDEOID").get(i));
                            information.setChannelTitle(intent.getBundleExtra("Data").getStringArrayList("CHANNELTITLE").get(i));
                            information.setDuration(intent.getBundleExtra("Data").getStringArrayList("DURATION").get(i));
                            information.setPublishedAt(intent.getBundleExtra("Data").getStringArrayList("PUBLISHEDAT").get(i));
                            information.setThumbnailUrl("https://i.ytimg.com/vi/" + intent.getBundleExtra("Data").getStringArrayList("VIDEOID").get(i) + "/hqdefault.jpg");
                            informations.add(information);
                        }

                        MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter(informations, MainActivity.this);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(myRecyclerViewAdapter);
                        recyclerView.removeOnItemTouchListener(recyclerTouchListener);
                        recyclerTouchListener = new RecyclerTouchListener(MainActivity.this, recyclerView, new RecyclerTouchListener.ClickListener() {
                            @Override
                            public void onClick(View view, final int position) {
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    new AlertDialog.Builder(MainActivity.this).setTitle("Download???").setMessage("Do You really want to download the Audio or Video  of  \n" + informations.get(position).getTitle() + "\nPlease confirm").setPositiveButton("Audio Download", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeAudioDownloadLink.class);
                                            download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                            download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                            download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                            download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                            Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                            startService(download_intent);

                                        }
                                    }).setNeutralButton("Video Download", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeVideoDownloadLinkCustomSettings.class);
                                            download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                            download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                            download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                            download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                            Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                            startService(download_intent);
                                        }
                                    }).setNegativeButton("Watch Video", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //watchYoutubeVideo(informations.get(position).getId());
                                            Intent intent1 = new Intent(MainActivity.this, MuxingAudioVideoInSeperateThred.class);
                                            intent1.putExtra("TITLE", informations.get(position).getTitle());
                                            startService(intent1);
                                        }
                                    }).show();
                                } else {
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                        new AlertDialog.Builder(MainActivity.this).setTitle("Download???").setMessage("Do You really want to download the Audio or Video  of  \n" + informations.get(position).getTitle() + "\nPlease confirm").setPositiveButton("Audio Download", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeAudioDownloadLink.class);
                                                download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                                download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                                download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                                download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                                Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                                startService(download_intent);

                                            }
                                        }).setNeutralButton("Video Download", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeVideoDownloadLinkCustomSettings.class);
                                                download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                                download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                                download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                                download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                                Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                                startService(download_intent);
                                            }
                                        }).setNegativeButton("Watch Video", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //watchYoutubeVideo(informations.get(position).getId());
                                                Intent intent1 = new Intent(MainActivity.this, MuxingAudioVideoInSeperateThred.class);
                                                intent1.putExtra("TITLE", informations.get(position).getTitle());
                                                startService(intent1);
                                            }
                                        }).show();
                                    } else {

                                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                            Toast.makeText(MainActivity.this, "To save audio or video write permission is required", Toast.LENGTH_LONG).show();
                                        }

                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);

                                    }

                                }
                            }

                            @Override
                            public void onLongClick(View view, int position) {


                            }
                        });
                        recyclerView.addOnItemTouchListener(recyclerTouchListener);
                    } else {
                        animator.end();
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        textView.setText("No Videos found");
                        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
                    }
                } else {
                    animator.end();
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    textView.setText("Youtube server is not responding. Try later please");
                    linearLayout.setGravity(Gravity.CENTER_VERTICAL);
                }

            } else if (intent.getAction().equals("com.asif.main_menu.activity.playlist")) {
                if (!intent.getBooleanExtra("isError", true)) {
                    if (intent.getBundleExtra("Data").getStringArrayList("TITLE").size() > 0) {
                        Toast.makeText(MainActivity.this, "Your total Created Playlist number is " + String.valueOf(intent.getBundleExtra("Data").getStringArrayList("TITLE").size()), Toast.LENGTH_SHORT).show();
                        animator.end();
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        textView.setText(intent.getStringExtra("name"));
                        name = intent.getStringExtra("name");
                        informations = new ArrayList<>();
                        int info_size = intent.getBundleExtra("Data").getStringArrayList("TITLE").size();
                        for (int i = 0; i < info_size; i++) {
                            Information information = new Information();
                            information.setTitle(intent.getBundleExtra("Data").getStringArrayList("TITLE").get(i));
                            information.setId(intent.getBundleExtra("Data").getStringArrayList("PLAYLISTID").get(i));
                            informations.add(information);
                        }

                        MyRecyclerViewAdapterForPlaylistView myRecyclerViewAdapter = new MyRecyclerViewAdapterForPlaylistView(informations);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(myRecyclerViewAdapter);

                        recyclerView.removeOnItemTouchListener(recyclerTouchListener);
                        recyclerTouchListener = new RecyclerTouchListener(MainActivity.this, recyclerView, new RecyclerTouchListener.ClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                                isLoadPlaylist = true;
                                Intent playlist_videos_intent = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                                playlist_videos_intent.putExtra("playlist_id", informations.get(position).getId());
                                playlist_videos_intent.putExtra("name", informations.get(position).getTitle());

                                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                                refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                                linearLayout.setGravity(Gravity.CENTER);
                                animator.setDuration(2000);
                                animator.setRepeatCount(ObjectAnimator.INFINITE);
                                animator.setRepeatMode(ObjectAnimator.RESTART);
                                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                                animator.start();

                                startService(playlist_videos_intent);
                            }

                            @Override
                            public void onLongClick(View view, final int position) {

                                new AlertDialog.Builder(MainActivity.this).setTitle("Confirm Your playlist").setMessage("Do You want to load \"" + informations.get(position).getTitle() + "\" playlist at startup of this app?").setPositiveButton("Yes, Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sharedPreferences.edit().putString("UserPlaylistId", informations.get(position).getId()).putString("UserPlaylistName", informations.get(position).getTitle()).apply();
                                        if (sharedPreferences.getString("UserPlaylistId", null) != null) {
                                            Snackbar.make(drawer, "Playlist successfully saved", Snackbar.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Playlist saving is interrupted", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }).setNegativeButton("No, Not now", null).show();

                            }
                        });
                        recyclerView.addOnItemTouchListener(recyclerTouchListener);
                    } else {
                        animator.end();
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        textView.setText("No Playlist found");
                        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
                    }
                } else {
                    animator.end();
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    textView.setText("Youtube server is not responding. Try later please");
                    linearLayout.setGravity(Gravity.CENTER_VERTICAL);
                }


            } else if (intent.getAction().equals("com.asif.activity.youtubesearch")) {
                if (!intent.getBooleanExtra("isError", true)) {
                    if (intent.getBundleExtra("Data").getStringArrayList("TITLE").size() > 0) {
                        animator.end();
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        name = intent.getStringExtra("name");
                        informations = new ArrayList<>();
                        int info_size = intent.getBundleExtra("Data").getStringArrayList("CHANNELTITLE").size();
                        for (int i = 0; i < info_size; i++) {
                            Information information = new Information();
                            information.setTitle(intent.getBundleExtra("Data").getStringArrayList("TITLE").get(i));
                            information.setId(intent.getBundleExtra("Data").getStringArrayList("VIDEOID").get(i));
                            information.setChannelTitle(intent.getBundleExtra("Data").getStringArrayList("CHANNELTITLE").get(i));
                            information.setDuration(intent.getBundleExtra("Data").getStringArrayList("DURATION").get(i));
                            information.setPublishedAt(intent.getBundleExtra("Data").getStringArrayList("PUBLISHEDAT").get(i));
                            information.setThumbnailUrl("https://i.ytimg.com/vi/" + intent.getBundleExtra("Data").getStringArrayList("VIDEOID").get(i) + "/hqdefault.jpg");
                            informations.add(information);
                        }

                        MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter(informations, MainActivity.this);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(myRecyclerViewAdapter);
                        recyclerView.removeOnItemTouchListener(recyclerTouchListener);
                        recyclerTouchListener = new RecyclerTouchListener(MainActivity.this, recyclerView, new RecyclerTouchListener.ClickListener() {
                            @Override
                            public void onClick(View view, final int position) {
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    new AlertDialog.Builder(MainActivity.this).setTitle("Download???").setMessage("Do You really want to download the Audio or Video  of  \n" + informations.get(position).getTitle() + "\nPlease confirm").setPositiveButton("Audio Download", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeAudioDownloadLink.class);
                                            download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                            download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                            download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                            download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                            Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                            startService(download_intent);

                                        }
                                    }).setNeutralButton("Video Download", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeVideoDownloadLinkCustomSettings.class);
                                            download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                            download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                            download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                            download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                            Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                            startService(download_intent);
                                        }
                                    }).setNegativeButton("Watch Video", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            watchYoutubeVideo(informations.get(position).getId());
                                        }
                                    }).show();
                                } else {
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                        new AlertDialog.Builder(MainActivity.this).setTitle("Download???").setMessage("Do You really want to download the Audio or Video  of  \n" + informations.get(position).getTitle() + "\nPlease confirm").setPositiveButton("Audio Download", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeAudioDownloadLink.class);
                                                download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                                download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                                download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                                download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                                Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                                startService(download_intent);

                                            }
                                        }).setNeutralButton("Video Download", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeVideoDownloadLinkCustomSettings.class);
                                                download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                                download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                                download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                                download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                                Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                                startService(download_intent);
                                            }
                                        }).setNegativeButton("Watch Video", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                watchYoutubeVideo(informations.get(position).getId());
                                            }
                                        }).show();
                                    } else {

                                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                            Toast.makeText(MainActivity.this, "To save audio or video write permission is required", Toast.LENGTH_LONG).show();
                                        }
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                                    }
                                }
                            }

                            @Override
                            public void onLongClick(View view, int position) {


                            }
                        });
                        recyclerView.addOnItemTouchListener(recyclerTouchListener);
                    } else {
                        animator.end();
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        textView.setText("No Videos found");
                        linearLayout.setGravity(Gravity.CENTER_VERTICAL);

                        Toast.makeText(MainActivity.this, "No videos found", Toast.LENGTH_LONG).show();
                    }
                } else {
                    animator.end();
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    textView.setText("Youtube server is not responding. Try later please");
                    linearLayout.setGravity(Gravity.CENTER_VERTICAL);
                }


            } else if (intent.getAction().equals("com.asif.activity.youtubemostpopular")) {
                if (!intent.getBooleanExtra("isError", true)) {
                    if (intent.getBundleExtra("Data").getStringArrayList("TITLE").size() > 0) {
                        animator.end();
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        informations = new ArrayList<>();
                        int info_size = intent.getBundleExtra("Data").getStringArrayList("CHANNELTITLE").size();
                        for (int i = 0; i < info_size; i++) {
                            Information information = new Information();
                            information.setTitle(intent.getBundleExtra("Data").getStringArrayList("TITLE").get(i));
                            information.setId(intent.getBundleExtra("Data").getStringArrayList("VIDEOID").get(i));
                            information.setChannelTitle(intent.getBundleExtra("Data").getStringArrayList("CHANNELTITLE").get(i));
                            information.setDuration(intent.getBundleExtra("Data").getStringArrayList("DURATION").get(i));
                            information.setPublishedAt(intent.getBundleExtra("Data").getStringArrayList("PUBLISHEDAT").get(i));
                            information.setThumbnailUrl("https://i.ytimg.com/vi/" + intent.getBundleExtra("Data").getStringArrayList("VIDEOID").get(i) + "/hqdefault.jpg");
                            informations.add(information);
                        }

                        MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter(informations, MainActivity.this);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(myRecyclerViewAdapter);
                        recyclerView.removeOnItemTouchListener(recyclerTouchListener);
                        recyclerTouchListener = new RecyclerTouchListener(MainActivity.this, recyclerView, new RecyclerTouchListener.ClickListener() {
                            @Override
                            public void onClick(View view, final int position) {
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    new AlertDialog.Builder(MainActivity.this).setTitle("Download???").setMessage("Do You really want to download the Audio or Video  of  \n" + informations.get(position).getTitle() + "\nPlease confirm").setPositiveButton("Audio Download", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeAudioDownloadLink.class);
                                            download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                            download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                            download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                            download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                            Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                            startService(download_intent);

                                        }
                                    }).setNeutralButton("Video Download", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeVideoDownloadLinkCustomSettings.class);
                                            download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                            download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                            download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                            download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                            Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                            startService(download_intent);
                                        }
                                    }).setNegativeButton("Watch Video", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            watchYoutubeVideo(informations.get(position).getId());
                                        }
                                    }).show();
                                } else {
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                        new AlertDialog.Builder(MainActivity.this).setTitle("Download???").setMessage("Do You really want to download the Audio or Video  of  \n" + informations.get(position).getTitle() + "\nPlease confirm").setPositiveButton("Audio Download", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeAudioDownloadLink.class);
                                                download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                                download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                                download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                                download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                                Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                                startService(download_intent);

                                            }
                                        }).setNeutralButton("Video Download", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent download_intent = new Intent(MainActivity.this, GenerateYoutubeVideoDownloadLinkCustomSettings.class);
                                                download_intent.putExtra("TITLE", informations.get(position).getTitle());
                                                download_intent.putExtra("VIDEOID", informations.get(position).getId());
                                                download_intent.putExtra("NOTIFICATIONO", getNotificationNo(informations.get(position).getId()));
                                                download_intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                                Snackbar.make(drawer, "Please check Your notification after a while", Snackbar.LENGTH_LONG).show();
                                                startService(download_intent);
                                            }
                                        }).setNegativeButton("Watch Video", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                watchYoutubeVideo(informations.get(position).getId());
                                            }
                                        }).show();
                                    } else {

                                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                            Toast.makeText(MainActivity.this, "To save audio or video write permission is required", Toast.LENGTH_LONG).show();
                                        }

                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);

                                    }

                                }
                            }

                            @Override
                            public void onLongClick(View view, int position) {


                            }
                        });
                        recyclerView.addOnItemTouchListener(recyclerTouchListener);
                    } else {
                        animator.end();
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        textView.setText("No Videos found");
                        linearLayout.setGravity(Gravity.CENTER_VERTICAL);

                        Toast.makeText(MainActivity.this, "No videos found", Toast.LENGTH_LONG).show();
                    }
                } else {
                    animator.end();
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    textView.setText("Youtube server is not responding. Try later please");
                    linearLayout.setGravity(Gravity.CENTER_VERTICAL);
                }

            } else if (intent.getAction().equals("com.asif.activity.tokenintent")) {
                if (sharedPreferences.getString("UserPlaylistId", null) != null) {
                    Intent token = new Intent(MainActivity.this, GetYoutubePlaylistVideosByIntentService.class);
                    token.putExtra("playlist_id", sharedPreferences.getString("UserPlaylistId", null));
                    token.putExtra("name", sharedPreferences.getString("UserPlaylistName", null));
                    startService(token);
                } else {
                    Intent most_popular = new Intent(MainActivity.this, GetYoutubeMostPopularVideosByIntentService.class);
                    most_popular.putExtra("VIDEO_CATAGORY", sharedPreferences.getString("VIDEO_CATAGORY", "28"));
                    startService(most_popular);
                }
            } else {
                animator.end();
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                refresh.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                textView.setText("Somethings is going wrong.Close app and start again please");
                linearLayout.setGravity(Gravity.CENTER_VERTICAL);
            }
        }

    }
}
