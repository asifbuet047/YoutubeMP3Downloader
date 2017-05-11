package com.asifbuetme.youtubemp3mp4downloader;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;


public class MuxingAudioVideoInSeperateThred extends IntentService {


    public MuxingAudioVideoInSeperateThred() {
        super("MuxingAudioVideoInSeperateThred");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
           /* File audio = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_LOCATION", "") + "/" + intent.getStringExtra("TITLE") + ".aac");
            File video = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_LOCATION", "") + "/" + intent.getStringExtra("TITLE") + ".h264");

            H264TrackImpl videoCodec = new H264TrackImpl(new FileDataSourceImpl(video));
            AACTrackImpl audioCodec = new AACTrackImpl(new FileDataSourceImpl(audio));

            AppendTrack appendTrack = new AppendTrack(videoCodec, audioCodec);
            Movie movie = new Movie();
            movie.addTrack(appendTrack);

            Container container = new DefaultMp4Builder().build(movie);

            FileChannel fc = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_LOCATION", "") + "/output.mp4")).getChannel();
            container.writeContainer(fc);
            fc.close();*/
            FFmpeg fFmpeg = FFmpeg.getInstance(MuxingAudioVideoInSeperateThred.this);
            Log.e("FFmpeg", fFmpeg.getDeviceFFmpegVersion());

        } catch (Exception e) {
            Log.e("Mux", e.toString());
        }

    }
}