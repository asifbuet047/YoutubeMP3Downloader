package com.asifbuetme.youtubemp3mp4downloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class GetYoutubeVideoDownloadLinkOfPreferResolutionByGoogle extends IntentService {


    String raw, download_link, clen, temp;

    public GetYoutubeVideoDownloadLinkOfPreferResolutionByGoogle() {
        super("GetYoutubeVideoDownloadLinkOfPreferResolutionByGoogle");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String title = intent.getStringExtra("TITLE");
        String youtube_video_id = intent.getStringExtra("VIDEOID");
        int notification_no = intent.getIntExtra("NOTIFICATIONO", 0);
        SharedPreferences sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        try {
            if (isNetworkAvailable()) {
                GetDataFromInternet internet = new GetDataFromInternet(GetYoutubeVideoDownloadLinkOfPreferResolutionByGoogle.this, "GET", 5000);
                internet.execute("https://www.youtube.com/watch?v=" + youtube_video_id);
                raw = internet.get();
                int start_index = raw.indexOf("itag%3D" + sharedPreferences.getString("VIDEO_RESOLUTION_ITAG", "160"), 0);
                if (start_index > 0) {
                    int first_index = raw.lastIndexOf(",", start_index);
                    int last_index = raw.indexOf(",", start_index);
                    temp = raw.substring(first_index + 1, last_index);
                    int clen_index = temp.indexOf("\\u0026clen=", 0);
                    if (clen_index > 0) {
                        clen = temp.substring(clen_index + "\\u0026clen=".length(), temp.indexOf("\\u0026", clen_index + "\\u0026clen=".length()));
                    } else {
                        clen = temp.substring(5, temp.indexOf("\\u0026", 0));
                    }
                    first_index = temp.indexOf("https%3A%2F%2Fr", 0);
                    last_index = temp.indexOf("\\u0026", first_index);
                    if (last_index > 0) {
                        download_link = temp.substring(first_index, last_index);
                    } else {
                        download_link = temp.substring(first_index);
                    }
                    download_link = download_link.replaceAll("%26", "&");
                    download_link = download_link.replaceAll("%3A", ":");
                    download_link = download_link.replaceAll("%2F", "/");
                    download_link = download_link.replaceAll("%3F", "?");
                    download_link = download_link.replaceAll("%3D", "=");
                    download_link = download_link.replaceAll("%252C", "%2C");
                    download_link = download_link.replaceAll("%252F", "%2F");
                    if (download_link.contains("clen")) {
                        download_link += "&range=0-" + clen;
                    } else {
                        download_link += "&clen=" + clen;
                        download_link += "&range=0-" + clen;
                    }
                    Notification.Builder builder = new Notification.Builder(GetYoutubeVideoDownloadLinkOfPreferResolutionByGoogle.this);
                    builder.setDefaults(Notification.DEFAULT_ALL);
                    builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
                    builder.setWhen(System.currentTimeMillis());
                    builder.setAutoCancel(true);
                    builder.setContentTitle(title);
                    builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.success)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
                    builder.setContentText("File-type:MP4 Size:" + String.valueOf(Long.parseLong(clen) / 1.0e6) + "MB Click to download the file");
                    Intent final_download_intent = new Intent(GetYoutubeVideoDownloadLinkOfPreferResolutionByGoogle.this, DownloadYoutubeAudioOrVideoAfterComfirmation.class);
                    final_download_intent.putExtra("LINK", download_link);
                    final_download_intent.putExtra("TITLE", title);
                    final_download_intent.putExtra("ISAUDIO", false);
                    builder.setContentIntent(PendingIntent.getService(GetYoutubeVideoDownloadLinkOfPreferResolutionByGoogle.this, 302, final_download_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                    notificationManager.notify(notification_no + 1, builder.build());
                } else {
                    Notification.Builder builder = new Notification.Builder(GetYoutubeVideoDownloadLinkOfPreferResolutionByGoogle.this);
                    builder.setDefaults(Notification.DEFAULT_ALL);
                    builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
                    builder.setWhen(System.currentTimeMillis());
                    builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.failure)).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
                    builder.setContentTitle("Don't exit desire resolution");
                    builder.setContentText("Your desire download resolution is not available for this video. Please select lower resolution in settings");
                    notificationManager.notify(420, builder.build());
                }
            }
        } catch (Exception e) {
            Log.e("Google", e.toString());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class GetDataFromInternet {

        private HttpsURLConnection client;
        private String response;
        private Context context;
        private String method;
        private int timeout;


        GetDataFromInternet(Context context, String method, int timeout) {
            this.method = method;
            this.timeout = timeout;
            this.context = context;
        }


        void execute(String uri) throws OutOfMemoryError, IOException {

            URL url = new URL(uri);
            client = (HttpsURLConnection) url.openConnection();
            if (method.equals("GET")) {
                client.setRequestMethod(method);
                client.setRequestProperty("authority", "www.youtube.com");
                client.setRequestProperty("method", "GET");
                client.setRequestProperty("scheme", "https");
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.connect();

                InputStream inputStream = client.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
                client.disconnect();
                response = stringBuilder.toString();
            } else if (method.equals("HEAD")) {
                client.setRequestMethod(method);
                client.setInstanceFollowRedirects(true);
                client.setRequestProperty("authority", "www.youtube.com");
                client.setRequestProperty("method", "GET");
                client.setRequestProperty("scheme", "https");
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.connect();
            }
        }

        String get() {
            return response;
        }
    }
}

/*    private void muxing() {

        String outputFile = "";

        try {

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "final2.mp4");
            file.createNewFile();
            outputFile = file.getAbsolutePath();

            MediaExtractor videoExtractor = new MediaExtractor();
            AssetFileDescriptor afdd = getAssets().openFd("Produce.MP4");
            videoExtractor.setDataSource(afdd.getFileDescriptor() ,afdd.getStartOffset(),afdd.getLength());

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioFilePath);

            Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount() );
            Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount() );

            MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = muxer.addTrack(audioFormat);

            Log.d(TAG, "Video Format " + videoFormat.toString() );
            Log.d(TAG, "Audio Format " + audioFormat.toString() );

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 100;
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS)
            {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    Log.d(TAG, "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                }
                else
                {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();


                    frameCount++;
                    Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

            Toast.makeText(getApplicationContext() , "frame:" + frameCount , Toast.LENGTH_SHORT).show();



            boolean sawEOS2 = false;
            int frameCount2 =0;
            while (!sawEOS2)
            {
                frameCount2++;

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    Log.d(TAG, "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                }
                else
                {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    audioBufferInfo.flags = audioExtractor.getSampleFlags();
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    audioExtractor.advance();


                    Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

            Toast.makeText(getApplicationContext() , "frame:" + frameCount2 , Toast.LENGTH_SHORT).show();

            muxer.stop();
            muxer.release();


        } catch (IOException e) {
            Log.d(TAG, "Mixer Error 1 " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Mixer Error 2 " + e.getMessage());
        }

}*/
