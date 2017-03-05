package com.example.asif.youtubemp3downloader;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class YoutubeFavouriteVideos extends ListActivity {


    private Bundle youtubeVideoInfo;
    DownloadManager downloadManager;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        youtubeVideoInfo = getIntent().getBundleExtra("Data");
        ArrayList<String> list = new ArrayList<>();
        list.add("NO Video Found");

        if (youtubeVideoInfo.getStringArrayList("TITLE") != null) {
            setListAdapter(new ArrayAdapter<>(this, R.layout.row_layout, R.id.title_view, youtubeVideoInfo.getStringArrayList("TITLE")));
        } else {
            setListAdapter(new ArrayAdapter<>(this, R.layout.row_layout, R.id.title_view, list));
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        title = youtubeVideoInfo.getStringArrayList("TITLE").get(position);
        new AlertDialog.Builder(YoutubeFavouriteVideos.this).setTitle("Do You want to download?").setMessage("Title:" + youtubeVideoInfo.getStringArrayList("TITLE").get(position) + "\nVideoID:" + youtubeVideoInfo.getStringArrayList("VIDEOID").get(position)).setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (youtubeVideoInfo.getStringArrayList("VIDEOID").get(position) != null) {
                    downloadYoutubeAudioByDownloadManager(youtubeVideoInfo.getStringArrayList("VIDEOID").get(position));
                }
            }
        }).setNegativeButton("Cancle", null).show();
        Toast.makeText(YoutubeFavouriteVideos.this, "OK", Toast.LENGTH_LONG).show();
    }

    private void downloadYoutubeAudioByDownloadManager(String youtube_video_id) {
       /* JSONObject jsonObject;
        String result, title = null, length;
        String url = getString(R.string.youtubeinmp3_link) + youtube_video_id;
        GetResponseFromInternet getResponseFromInternet = new GetResponseFromInternet(YoutubeFavouriteVideos.this, "GET", 2500);
        getResponseFromInternet.execute(url);*/

        try {
            /*result = getResponseFromInternet.get();
            jsonObject = new JSONObject(result);
            title = jsonObject.optString("title");
            length = jsonObject.optString("length");*/
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

        } catch (Exception e) {
            Toast.makeText(YoutubeFavouriteVideos.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
