package com.asifbuetme.youtubemp3mp4downloader;


import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.asifbuetme.youtubemp3mp4downloader.directorychooser.DialogProperties;
import com.asifbuetme.youtubemp3mp4downloader.directorychooser.DialogSelectionListener;
import com.asifbuetme.youtubemp3mp4downloader.directorychooser.DirectoryPicker;
import com.turkialkhateeb.materialcolorpicker.ColorChooserDialog;
import com.turkialkhateeb.materialcolorpicker.ColorListener;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {


    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 102;
    TwoStatePreference desiredPreference;
    CheckBoxPreference wifi_only, custom_folder_audio, custom_folder_video;
    SwitchPreference prefer_list, navigation_color, toolbar_color, theme;
    ListPreference audio_bitrate, video_resolution, video_catagories;
    Toolbar toolbar;
    AlarmManager alarmManager;


    public class SettingsFragment extends PreferenceFragment implements ColorListener {
        private String tone_uri;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
            Log.e("SettingsFragment", "onCreate()");

            wifi_only = (CheckBoxPreference) findPreference("wifi_only");
            custom_folder_audio = (CheckBoxPreference) findPreference("custom_folder_audio");
            custom_folder_video = (CheckBoxPreference) findPreference("custom_folder_video");
            prefer_list = (SwitchPreference) findPreference("prefer_list");
            audio_bitrate = (ListPreference) findPreference("audio_bitrate");
            video_resolution = (ListPreference) findPreference("video_resolution");
            video_catagories = (ListPreference) findPreference("video_catagories");
            navigation_color = (SwitchPreference) findPreference("navigation_color");
            toolbar_color = (SwitchPreference) findPreference("toolbar_color");
            theme = (SwitchPreference) findPreference("theme");

            wifi_only.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue) {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putBoolean("WIFI_ONLY", (Boolean) newValue).apply();
                        wifi_only.setIcon(R.drawable.ic_network_wifi_black_24dp);
                        Toast.makeText(getActivity(), "Download starts only in wifi", Toast.LENGTH_SHORT).show();

                    } else {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putBoolean("WIFI_ONLY", (Boolean) newValue).apply();
                        wifi_only.setIcon(R.drawable.ic_network_cell_black_24dp);
                        Toast.makeText(getActivity(), "Download starts both in wifi and data", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            custom_folder_audio.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue) {
                        desiredPreference = (TwoStatePreference) preference;
                        if ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                showDirectoryPicker(true, custom_folder_audio);
                            } else {
                                Toast.makeText(SettingsActivity.this, "Memory card is not available", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                        showDirectoryPicker(true, custom_folder_audio);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Memory card is not available", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    custom_folder_audio.setChecked(false);
                                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                                }
                            }
                        }
                    } else {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("AUDIO_LOCATION", Environment.DIRECTORY_MUSIC).apply();
                        preference.setSummary("Location: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("AUDIO_LOCATION", null));

                    }
                    return true;
                }
            });
            custom_folder_video.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue) {
                        desiredPreference = (TwoStatePreference) preference;
                        if ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                showDirectoryPicker(false, custom_folder_video);
                            } else {
                                Toast.makeText(SettingsActivity.this, "Memory card is not available", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                        showDirectoryPicker(false, custom_folder_video);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Memory card is not available", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    custom_folder_video.setChecked(false);
                                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                                }
                            }
                        }
                    } else {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("VIDEO_LOCATION", Environment.DIRECTORY_MOVIES).apply();
                        preference.setSummary("Location: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_LOCATION", null));

                    }
                    return true;
                }
            });
            prefer_list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue) {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putBoolean("PREFER_LIST", (Boolean) newValue).apply();
                        Toast.makeText(getActivity(), "Load prefer category videos at app launch", Toast.LENGTH_SHORT).show();
                    } else {
                        if (getSharedPreferences("AppPref", MODE_PRIVATE).getString("UserPlaylistId", null) != null) {
                            getSharedPreferences("AppPref", MODE_PRIVATE).edit().putBoolean("PREFER_LIST", (Boolean) newValue).apply();
                            Toast.makeText(getActivity(), "Load Your save playlist videos at app launch", Toast.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(getView(), "Please select one of Your playlist to load that at app launch. To save playlist just press long click on any of Your playlist until a dialog window will come and click ok", Snackbar.LENGTH_LONG).show();
                        }
                    }
                    return true;
                }
            });
            audio_bitrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("AUDIO_BITRATE", (String) newValue).apply();
                    audio_bitrate.setSummary("Current bitrate: " + newValue + "kbit/s");
                    Toast.makeText(SettingsActivity.this, "Audio bitrate " + newValue + "kbit/s is selected", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            video_resolution.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    List<String> values = Arrays.asList(getResources().getStringArray(R.array.list_preference_resolution_values));
                    getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("VIDEO_RESOLUTION", (String) newValue).putString("VIDEO_RESOLUTION_ITAG", getResources().getStringArray(R.array.list_preference_resolution_itags)[values.indexOf(newValue)]).apply();
                    video_resolution.setSummary("Current resolution: " + newValue);
                    Toast.makeText(SettingsActivity.this, "Video resolution " + getResources().getStringArray(R.array.list_preference_resolution_itags)[values.indexOf(newValue)] + " is selected", Toast.LENGTH_SHORT).show();

                    return true;
                }
            });
            video_catagories.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String value = (String) newValue;
                    List<String> catagories_id = Arrays.asList(getResources().getStringArray(R.array.list_youtube_catagories_id));
                    getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("VIDEO_CATAGORY", value).apply();
                    video_catagories.setSummary(getResources().getStringArray(R.array.list_youtube_catagories)[catagories_id.indexOf(value)]);
                    Toast.makeText(SettingsActivity.this, "Video catagory \"" + getResources().getStringArray(R.array.list_youtube_catagories)[catagories_id.indexOf(value)] + "\" is selected", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            navigation_color.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue) {
                        ColorChooserDialog colorChooserDialog = new ColorChooserDialog(SettingsActivity.this);
                        colorChooserDialog.setTitle("Choose Your color Navigation drawer");
                        colorChooserDialog.setColorListener(new ColorListener() {
                            @Override
                            public void OnColorClick(View v, int color) {
                                getSharedPreferences("AppPref", MODE_PRIVATE).edit().putInt("NAVIGATION_COLOR", color).apply();
                                navigation_color.setChecked(true);
                            }
                        });
                        colorChooserDialog.setCanceledOnTouchOutside(false);
                        colorChooserDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                navigation_color.setChecked(false);
                            }
                        });
                        colorChooserDialog.show();
                    } else {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putInt("NAVIGATION_COLOR", ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary)).apply();
                        Toast.makeText(SettingsActivity.this, "Default color is used", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            });
            toolbar_color.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue) {
                        ColorChooserDialog colorChooserDialog = new ColorChooserDialog(SettingsActivity.this);
                        colorChooserDialog.setTitle("Choose Your color for Toolbar");
                        colorChooserDialog.setColorListener(new ColorListener() {
                            @Override
                            public void OnColorClick(View v, int color) {
                                getSharedPreferences("AppPref", MODE_PRIVATE).edit().putInt("TOOLBAR_COLOR", color).apply();
                                toolbar_color.setChecked(true);
                                toolbar.setBackgroundColor(color);
                            }
                        });
                        colorChooserDialog.setCanceledOnTouchOutside(false);
                        colorChooserDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                toolbar_color.setChecked(false);
                            }
                        });
                        colorChooserDialog.show();
                    } else {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putInt("TOOLBAR_COLOR", ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent)).apply();
                        toolbar.setBackgroundColor(ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
                        Toast.makeText(SettingsActivity.this, "Default color is used", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            });
            theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean) newValue) {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putBoolean("DARK_THEME", true).putInt("TOOLBAR_COLOR", ContextCompat.getColor(SettingsActivity.this, R.color.primary_dark)).putInt("NAVIGATION_COLOR", ContextCompat.getColor(SettingsActivity.this, R.color.accent)).apply();
                        Snackbar.make(getView(), "Dark theme is selected. App will be restarted soon. Please wait", Snackbar.LENGTH_LONG).show();
                        toolbar.setBackgroundColor(ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
                        theme.setIcon(R.drawable.ic_dark_black_24dp);
                        MainActivity.getInstance().finish();
                        alarmManager = (AlarmManager) SettingsActivity.this.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, PendingIntent.getActivity(SettingsActivity.this, 570996, new Intent(SettingsActivity.this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT));
                    } else {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putBoolean("DARK_THEME", false).putInt("TOOLBAR_COLOR", ContextCompat.getColor(SettingsActivity.this, R.color.materialcolorpicker__red)).putInt("NAVIGATION_COLOR", ContextCompat.getColor(SettingsActivity.this, R.color.materialcolorpicker__dialogcolor)).apply();
                        toolbar.setBackgroundColor(ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
                        Snackbar.make(getView(), "Light theme is selected. App will be restarted soon. Please wait", Snackbar.LENGTH_LONG).show();
                        theme.setIcon(R.drawable.ic_light_black_24dp);
                        MainActivity.getInstance().finish();
                        alarmManager = (AlarmManager) SettingsActivity.this.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, PendingIntent.getActivity(SettingsActivity.this, 570996, new Intent(SettingsActivity.this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT));
                    }
                    return true;
                }
            });


        }

        @Override
        public void onResume() {
            super.onResume();
            Log.e("SettingsFragment", "onResume()");
            custom_folder_audio.setSummary("Location: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("AUDIO_LOCATION", "Invalid"));
            custom_folder_video.setSummary("Location: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_LOCATION", "Invalid"));
            audio_bitrate.setSummary("Current bitrate: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("AUDIO_BITRATE", "Invalid") + " kbit/s");
            video_resolution.setSummary("Current resolution: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_RESOLUTION", "Invalid"));
            List<String> catagories_id = Arrays.asList(getResources().getStringArray(R.array.list_youtube_catagories_id));
            video_catagories.setSummary(getResources().getStringArray(R.array.list_youtube_catagories)[catagories_id.indexOf(getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_CATAGORY", "28"))]);


            if (getSharedPreferences("AppPref", MODE_PRIVATE).getBoolean("WIFI_ONLY", false)) {
                wifi_only.setIcon(R.drawable.ic_network_wifi_black_24dp);
            } else {
                wifi_only.setIcon(R.drawable.ic_network_cell_black_24dp);
            }
            if (getSharedPreferences("AppPref", MODE_PRIVATE).getBoolean("SHOW_NOTIFICATION", true)) {
                prefer_list.setIcon(R.drawable.ic_notifications_black_24dp);
            } else {
                prefer_list.setIcon(R.drawable.ic_notifications_off_black_24dp);
            }
            if (getSharedPreferences("AppPref", MODE_PRIVATE).getBoolean("DARK_THEME", true)) {
                theme.setIcon(R.drawable.ic_dark_black_24dp);
            } else {
                theme.setIcon(R.drawable.ic_light_black_24dp);
            }
            toolbar.setBackgroundColor(getSharedPreferences("AppPref", MODE_PRIVATE).getInt("TOOLBAR_COLOR", ContextCompat.getColor(SettingsActivity.this, R.color.materialcolorpicker__grey)));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.e("SettingsFragment", "onDestroy()");
        }

        @Override
        public void OnColorClick(View v, int color) {
            Toast.makeText(SettingsActivity.this, String.valueOf(color), Toast.LENGTH_LONG).show();

        }

        private void playSound(Context context, Uri alert) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(context, alert);
                final AudioManager audioManager = (AudioManager) context
                        .getSystemService(Context.AUDIO_SERVICE);
                if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
            } catch (IOException e) {
                Log.e("Player", e.toString());
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSharedPreferences("AppPref", MODE_PRIVATE).getBoolean("DARK_THEME", true)) {
            setTheme(R.style.MyDarkTheme);
        } else {
            setTheme(R.style.MyLightTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().add(R.id.content_frame, new SettingsFragment(), "Settings").commit();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showDirectoryPicker(final boolean isAudio, final TwoStatePreference preference) {
        final String[] location = new String[1];
        DialogProperties dialogProperties = new DialogProperties();
        dialogProperties.selection_type = com.asifbuetme.youtubemp3mp4downloader.DialogConfigs.DIR_SELECT;
        dialogProperties.selection_mode = com.asifbuetme.youtubemp3mp4downloader.DialogConfigs.SINGLE_MODE;
        dialogProperties.root = new File(com.asifbuetme.youtubemp3mp4downloader.DialogConfigs.DEFAULT_DIR);
        dialogProperties.error_dir = new File(com.asifbuetme.youtubemp3mp4downloader.DialogConfigs.DEFAULT_DIR);
        dialogProperties.offset = new File(com.asifbuetme.youtubemp3mp4downloader.DialogConfigs.DEFAULT_DIR);
        dialogProperties.extensions = null;

        final DirectoryPicker directoryPicker = new DirectoryPicker(SettingsActivity.this, dialogProperties);
        directoryPicker.setCanceledOnTouchOutside(false);
        if (isAudio) {
            directoryPicker.setTitle("Choose Audio download directory");
        } else {
            directoryPicker.setTitle("Choose Video download directory");
        }
        directoryPicker.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                try {
                    if (files[0].length() >= 1) {
                        Toast.makeText(SettingsActivity.this, files[0], Toast.LENGTH_LONG).show();
                        location[0] = files[0];
                        preference.setChecked(true);
                        directoryPicker.cancel();
                        Toast.makeText(SettingsActivity.this, files[0], Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    preference.setChecked(false);
                    Toast.makeText(SettingsActivity.this, "You choose nothing", Toast.LENGTH_LONG).show();
                }
            }
        });
        directoryPicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (isAudio) {
                    if (location[0] == null) {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("AUDIO_LOCATION", File.separator + Environment.DIRECTORY_MUSIC).apply();
                        preference.setSummary("Location: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("AUDIO_LOCATION", null));
                        preference.setChecked(false);
                    } else {
                        String path = location[0];
                        path = path.substring(7);
                        preference.setSummary("Location: " + path);
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("AUDIO_LOCATION", path).apply();
                    }
                } else {
                    if (location[0] == null) {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("AUDIO_LOCATION", File.separator + Environment.DIRECTORY_MOVIES).apply();
                        preference.setSummary("Location: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_LOCATION", null));
                        preference.setChecked(false);
                    } else {
                        String path = location[0];
                        path = path.substring(7);
                        preference.setSummary("Location: " + path);
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("VIDEO_LOCATION", path).apply();
                    }
                }

            }
        });
        directoryPicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isAudio) {
                    if (location[0] == null) {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("AUDIO_LOCATION", File.separator + Environment.DIRECTORY_MUSIC).apply();
                        preference.setSummary("Location: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("AUDIO_LOCATION", null));
                        preference.setChecked(false);
                    } else {
                        String path = location[0];
                        path = path.substring(7);
                        preference.setSummary("Location: " + path);
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("AUDIO_LOCATION", path).apply();
                    }
                } else {
                    if (location[0] == null) {
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("VIDEO_LOCATION", File.separator + Environment.DIRECTORY_MOVIES).apply();
                        preference.setSummary("Location: " + getSharedPreferences("AppPref", MODE_PRIVATE).getString("VIDEO_LOCATION", null));
                        preference.setChecked(false);
                    } else {
                        String path = location[0];
                        path = path.substring(7);
                        preference.setSummary("Location: " + path);
                        getSharedPreferences("AppPref", MODE_PRIVATE).edit().putString("VIDEO_LOCATION", path).apply();
                    }
                }

            }
        });
        directoryPicker.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SettingsActivity.this, "Permission is granted", Toast.LENGTH_LONG).show();
                    desiredPreference.setChecked(false);
                } else {
                    desiredPreference.setChecked(false);
                    Toast.makeText(SettingsActivity.this, "Permission is Required for getting list of files", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}