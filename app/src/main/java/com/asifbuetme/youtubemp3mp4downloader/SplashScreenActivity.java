package com.asifbuetme.youtubemp3mp4downloader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;

import com.asifbuetme.youtubemp3mp4downloader.R;

public class SplashScreenActivity extends AppCompatActivity {

    ProgressBar progressBar;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        progressBar = (ProgressBar) findViewById(R.id.splash_progress_bar);
        progressBar.setInterpolator(new AccelerateInterpolator());
        progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        new Thread(new Runnable() {
            int progress = 0;

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Log.e("Splash", e.toString());
                    }
                    progress++;
                    progressBar.setProgress(progress);
                    if (progress == 100) {
                        if (sharedPreferences.getBoolean("FIRST_TIME", true)) {
                            startActivity(new Intent(SplashScreenActivity.this, WelcomeScreenActivity.class));
                        } else {
                            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
