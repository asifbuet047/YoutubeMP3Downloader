package com.asifbuetme.youtubemp3mp4downloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asifbuetme.youtubemp3mp4downloader.R;

import java.io.InputStream;
import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Information> data;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, channelTitle, publishedAt, duration;
        ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.data_thumbnail);
            title = (TextView) view.findViewById(R.id.data_title);
            channelTitle = (TextView) view.findViewById(R.id.data_channelTitle);
            duration = (TextView) view.findViewById(R.id.data_duration);
            publishedAt = (TextView) view.findViewById(R.id.data_publishedAt);
            if (context.getSharedPreferences("AppPref", Context.MODE_PRIVATE).getBoolean("DARK_THEME", true)) {
                title.setTextColor(context.getResources().getColor(R.color.materialcolorpicker__white));
                channelTitle.setTextColor(context.getResources().getColor(R.color.materialcolorpicker__white));
                duration.setTextColor(context.getResources().getColor(R.color.materialcolorpicker__white));
                publishedAt.setTextColor(context.getResources().getColor(R.color.materialcolorpicker__white));
            }
        }
    }


    MyRecyclerViewAdapter(ArrayList<Information> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_row_layout_2, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Information information = data.get(position);
        new DownloadImageTask(holder.thumbnail).execute(information.getThumbnailUrl());
        holder.title.setText(information.getTitle());
        holder.channelTitle.setText(information.getChannelTitle());
        holder.duration.setText(formatDuration(information.getDuration()));
        holder.publishedAt.setText(formatDate(information.getPublishedAt()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String formatDuration(String isoFormat) {
        String duration;
        if (isoFormat.indexOf('H', 0) > 0) {
            if (isoFormat.indexOf('M', 0) > 0) {
                if (isoFormat.indexOf('S', 0) > 0) {
                    duration = isoFormat.substring(2, isoFormat.indexOf('H', 0)) + ":";
                    duration += isoFormat.substring(isoFormat.indexOf('H', 0) + 1, isoFormat.indexOf('M', 0)) + ":";
                    duration += isoFormat.substring(isoFormat.indexOf('M', 0) + 1, isoFormat.indexOf('S', 0));
                } else {
                    duration = isoFormat.substring(2, isoFormat.indexOf('H', 0)) + ":";
                    duration += isoFormat.substring(isoFormat.indexOf('H', 0) + 1, isoFormat.indexOf('M', 0)) + ":";
                    duration += "00";
                }
            } else {
                if (isoFormat.indexOf('S', 0) > 0) {
                    duration = isoFormat.substring(2, isoFormat.indexOf('H', 0)) + ":";
                    duration += "00:";
                    duration += isoFormat.substring(isoFormat.indexOf('H', 0) + 1, isoFormat.indexOf('S', 0));
                } else {
                    duration = isoFormat.substring(2, isoFormat.indexOf('H', 0)) + ":";
                    duration += "00:00";
                }

            }
        } else if (isoFormat.indexOf('M', 0) > 0) {
            if (isoFormat.indexOf('S', 0) > 0) {
                duration = isoFormat.substring(2, isoFormat.indexOf('M', 0)) + ":";
                duration += isoFormat.substring(isoFormat.indexOf('M', 0) + 1, isoFormat.indexOf('S', 0));
            } else {
                duration = isoFormat.substring(2, isoFormat.indexOf('M', 0)) + ":00";
            }
        } else {
            duration = isoFormat.substring(2, isoFormat.indexOf('S', 0));
        }
        return duration;
    }

    private String formatDate(String isoFormat) {
        String date;
        date = isoFormat.substring(0, isoFormat.indexOf('T', 0));
        return date;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("DownloadImageTask", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
