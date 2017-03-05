package com.example.asif.youtubemp3downloader;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.simple.SwingRightInAnimationAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class YoutubePlaylistVideoActivity extends ListActivity {

    Bundle youtubeVideoInfo;
    DownloadManager downloadManager;
    GetResponseFromInternet getResponseFromInternet;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        youtubeVideoInfo = getIntent().getBundleExtra("Data");
        ArrayList<String> list = new ArrayList<>();
        list.add("NO Video Found");

        /*LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(YoutubePlaylistVideoActivity.this);
        YoutubeDownloaderBroadcastReceiver receiver = new YoutubeDownloaderBroadcastReceiver();
        localBroadcastManager.registerReceiver(receiver, new IntentFilter("com.asif.youtubeplaylistvideoactivity.token"));*/

        if (youtubeVideoInfo.getStringArrayList("TITLE") != null) {
            for (int i = 0; i < youtubeVideoInfo.getStringArrayList("TITLE").size(); i++) {
                youtubeVideoInfo.getStringArrayList("TITLE").set(i, String.valueOf(i + 1) + "." + youtubeVideoInfo.getStringArrayList("TITLE").get(i));
            }
            UsersAdapter stringArrayAdapter = new UsersAdapter(youtubeVideoInfo.getStringArrayList("TITLE"));
            SwingRightInAnimationAdapter animationAdapter = new SwingRightInAnimationAdapter(stringArrayAdapter);
            animationAdapter.setAbsListView(getListView());
            setListAdapter(animationAdapter);
            Toast.makeText(YoutubePlaylistVideoActivity.this, "Total no of Playlist: " + String.valueOf(youtubeVideoInfo.getStringArrayList("TITLE").size()), Toast.LENGTH_LONG).show();
        } else {
            setListAdapter(new ArrayAdapter<>(this, R.layout.row_layout, R.id.title_view, list));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        title = youtubeVideoInfo.getStringArrayList("TITLE").get(position);
        new AlertDialog.Builder(YoutubePlaylistVideoActivity.this).setTitle("Do You want to download?").setMessage("Title:" + youtubeVideoInfo.getStringArrayList("TITLE").get(position) + "\nVideoID:" + youtubeVideoInfo.getStringArrayList("VIDEOID").get(position)).setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (youtubeVideoInfo.getStringArrayList("VIDEOID").get(position) != null) {
                    //downloadYoutubeAudioByDownloadManager(youtubeVideoInfo.getStringArrayList("VIDEOID").get(position));
                    Intent intent = new Intent(YoutubePlaylistVideoActivity.this, DownloadYoutubeAudioByDownloadManager.class);
                    intent.putExtra("TITLE", title);
                    intent.putExtra("VIDEOID", youtubeVideoInfo.getStringArrayList("VIDEOID").get(position));
                    startService(intent);
                }
            }
        }).setNegativeButton("Cancle", null).show();
    }


    private void downloadYoutubeAudioByDownloadManager(String youtube_video_id) throws JSONException, ExecutionException, InterruptedException, IOException {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" + title + ".mp3");
        if (!file.exists()) {
            getResponseFromInternet = new GetResponseFromInternet(YoutubePlaylistVideoActivity.this, "GET", 2000);
            getResponseFromInternet.setActionName("com.asif.youtubeplaylistvideoactivity.token");
            getResponseFromInternet.execute("https://www.youtubeinmp3.com/fetch/?format=json&video=http://www.youtube.com/watch?v=" + youtube_video_id);
            String response = getResponseFromInternet.get();
            if (getResponseFromInternet.client.getResponseCode() == 200) {
                JSONObject jsonObject = new JSONObject(response);
                String conversion_link = jsonObject.getString("link");
                getResponseFromInternet = new GetResponseFromInternet(YoutubePlaylistVideoActivity.this, "HEAD", 3000);
                getResponseFromInternet.execute(conversion_link);
                getResponseFromInternet.get();
                Toast.makeText(YoutubePlaylistVideoActivity.this, getResponseFromInternet.client.getHeaderField("Location"), Toast.LENGTH_LONG).show();
                String download_link = getResponseFromInternet.client.getHeaderField("Location");

                if (getResponseFromInternet.client.getResponseCode() == 302) {
                    getResponseFromInternet = new GetResponseFromInternet(YoutubePlaylistVideoActivity.this, "HEAD", 5000);
                    getResponseFromInternet.execute("https:" + download_link);
                    getResponseFromInternet.get();

                    if (getResponseFromInternet.client.getResponseCode() == 200) {
                        if (getResponseFromInternet.client.getHeaderField("Content-Type").equals("audio/mpeg")) {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https:" + download_link));
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
                            Toast.makeText(YoutubePlaylistVideoActivity.this, "File is not mp3 format...", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(YoutubePlaylistVideoActivity.this, "200 not found...Download cant be start", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(YoutubePlaylistVideoActivity.this, "302 not found...Download cant be start", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(YoutubePlaylistVideoActivity.this, "Check Your Internet connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(YoutubePlaylistVideoActivity.this, "Already exits...", Toast.LENGTH_SHORT).show();
        }

    }


    public class UsersAdapter extends ArrayAdapter<String> {

        public UsersAdapter(ArrayList<String> names) {
            super(YoutubePlaylistVideoActivity.this, 0, names);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String text = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(YoutubePlaylistVideoActivity.this).inflate(R.layout.content_list_view, parent, false);
            }
            TextView each = (TextView) convertView.findViewById(R.id.content_list_view_text);
            each.setText(text);
            return convertView;
        }
    }

    public class YoutubePlaylistVideoActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(YoutubePlaylistVideoActivity.this, "...", Toast.LENGTH_SHORT).show();
            switch (intent.getAction()) {
                case "com.asif.youtubeplaylistvideoactivity.token":
                    try {
                        if (getResponseFromInternet.client.getResponseCode() == 200) {
                            JSONObject jsonObject = new JSONObject(getResponseFromInternet.get());
                            String conversion_token = jsonObject.getString("link");
                            String video_length = jsonObject.getString("length");
                            Toast.makeText(YoutubePlaylistVideoActivity.this, conversion_token, Toast.LENGTH_SHORT).show();
                            Toast.makeText(YoutubePlaylistVideoActivity.this, video_length, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(YoutubePlaylistVideoActivity.this, "200 not found...", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

}
