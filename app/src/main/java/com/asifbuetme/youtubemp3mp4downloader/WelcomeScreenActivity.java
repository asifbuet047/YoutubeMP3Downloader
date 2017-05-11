package com.asifbuetme.youtubemp3mp4downloader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import java.io.IOException;

public class WelcomeScreenActivity extends AppCompatActivity implements View.OnClickListener {


    ImageView first_dot, second_dot, third_dot;
    Button sign_in_button;
    SharedPreferences sharedPreferences;
    ViewPager viewPager;
    LocalBroadcastManager localBroadcastManager;
    FirstAuthenticationReceiver firstAuthenticationReceiver;
    public static int REQUEST_CODE_PICK_ACCOUNT = 102;
    public static int REQUEST_CODE_AUTH = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        first_dot = (ImageView) findViewById(R.id.first_dot);
        second_dot = (ImageView) findViewById(R.id.second_dot);
        third_dot = (ImageView) findViewById(R.id.third_dot);
        sign_in_button = (Button) findViewById(R.id.sign_in_button);
        viewPager = (ViewPager) findViewById(R.id.container);
        sign_in_button.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("AppPref", MODE_PRIVATE);

        WelcomeScreenAdapter welcomeScreenAdapter = new WelcomeScreenAdapter(getSupportFragmentManager());
        viewPager.setAdapter(welcomeScreenAdapter);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    first_dot.setImageResource(R.drawable.selecteditem_dot);
                    second_dot.setImageResource(R.drawable.nonselecteditem_dot);
                    third_dot.setImageResource(R.drawable.nonselecteditem_dot);
                } else if (position == 1) {
                    first_dot.setImageResource(R.drawable.nonselecteditem_dot);
                    second_dot.setImageResource(R.drawable.selecteditem_dot);
                    third_dot.setImageResource(R.drawable.nonselecteditem_dot);
                } else if (position == 2) {
                    first_dot.setImageResource(R.drawable.nonselecteditem_dot);
                    second_dot.setImageResource(R.drawable.nonselecteditem_dot);
                    third_dot.setImageResource(R.drawable.selecteditem_dot);
                }
            }


            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        localBroadcastManager = LocalBroadcastManager.getInstance(WelcomeScreenActivity.this);
        firstAuthenticationReceiver = new FirstAuthenticationReceiver();
        IntentFilter intentFilter = new IntentFilter("com.asif.activity.tokenintent");
        localBroadcastManager.registerReceiver(firstAuthenticationReceiver, intentFilter);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_PICK_ACCOUNT) & ((data != null))) {
            sharedPreferences.edit().putString("Email", data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)).apply();
            sharedPreferences.edit().putString("Type", data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)).apply();
            Snackbar.make(viewPager, sharedPreferences.getString("Email", null) + " has been selected", Snackbar.LENGTH_SHORT).show();
            if (isOnline()) {
                GetGoogleApiTokenByAuthentication authentication = new GetGoogleApiTokenByAuthentication(WelcomeScreenActivity.this, "oauth2:https://www.googleapis.com/auth/youtube.readonly", sharedPreferences);
                authentication.execute();
            } else {
                Snackbar.make(viewPager, "No internet. Please check Your internet settings", Snackbar.LENGTH_LONG).show();
            }
        } else if ((requestCode == REQUEST_CODE_AUTH) & (resultCode == RESULT_OK) & (data != null)) {
            Toast.makeText(WelcomeScreenActivity.this, "Authentication for first time has been finished. Welcome!!!", Toast.LENGTH_LONG).show();
            finish();
            startActivity(new Intent(WelcomeScreenActivity.this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
            new AlertDialog.Builder(WelcomeScreenActivity.this).setTitle("Don't panic").setMessage("Log in to Your gmail account is necessary to show Your all playlist in Youtube. We only read Your contents. We even don't have the permission to edit Your contents in Youtube. So there is no cause for panic. Please Sign in and enjoy unlimited download as Your wish").setPositiveButton("Yes, I am in", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pickUserAccount();
                }
            }).setNegativeButton("No, I am not sure now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(WelcomeScreenActivity.this, "You can sign in at any time You want", Toast.LENGTH_LONG).show();
                }
            }).show();
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static class FirstScreen extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_first_screen, container, false);
        }
    }

    public static class SecondScreen extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_second_screen, container, false);
        }
    }

    public static class ThirdScreen extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_third_layout, container, false);
        }
    }

    private class WelcomeScreenAdapter extends FragmentPagerAdapter {

        WelcomeScreenAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FirstScreen();
                case 1:
                    return new SecondScreen();
                case 2:
                    return new ThirdScreen();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
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
                if (sharedPreferences.getBoolean("FIRST_TIME", true)) {
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("Access Token", token);
                    edit.putLong("Time of Token", System.currentTimeMillis());
                    edit.apply();
                    Intent token_intent = new Intent("com.asif.activity.tokenintent");
                    token_intent.getBooleanExtra("TOKEN", true);
                    localBroadcastManager.sendBroadcast(token_intent);
                }
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
                Snackbar.make(viewPager, "Authentication is completed", Snackbar.LENGTH_SHORT).show();
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

    private class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    private class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    private class FirstAuthenticationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.asif.activity.tokenintent")) {
                sharedPreferences.edit().putBoolean("FIRST_TIME", false).apply();
                Toast.makeText(WelcomeScreenActivity.this, "Authentication has been finished. Welcome again!!!", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(WelcomeScreenActivity.this, MainActivity.class));
            }
        }
    }
}
