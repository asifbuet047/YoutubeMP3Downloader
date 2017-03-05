package com.example.asif.youtubemp3downloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

/**
 * Created by ASIF on 12/22/2016.
 */

class GetGoogleApiTokenIfAuthenticated extends AsyncTask<String, String, String> {
    private String mEmail, mScope;
    private Context context;
    private SharedPreferences preferences;

    static int NO_ACCOUNT_EXISTENCE_CODE = 102;

    GetGoogleApiTokenIfAuthenticated(Context context, String email, String google_scope, SharedPreferences sharedPreferences) {
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
            if (!isAccessTokenValid()) {
                token = GoogleAuthUtil.getToken(
                        context,
                        mEmail,
                        mScope);
            }
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


    private boolean isAccessTokenValid() {
        return (System.currentTimeMillis() - preferences.getLong("Time of Token", 100)) < 3500000;
    }

}
