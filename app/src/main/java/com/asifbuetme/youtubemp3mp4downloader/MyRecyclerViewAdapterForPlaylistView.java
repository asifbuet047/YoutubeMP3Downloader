package com.asifbuetme.youtubemp3mp4downloader;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asifbuetme.youtubemp3mp4downloader.R;

import java.util.ArrayList;

/**
 * Created by ASIF on 3/21/2017.
 */

public class MyRecyclerViewAdapterForPlaylistView extends RecyclerView.Adapter<MyRecyclerViewAdapterForPlaylistView.MyViewHolder> {

    private ArrayList<Information> data;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
        }
    }

    MyRecyclerViewAdapterForPlaylistView(ArrayList<Information> data) {
        this.data = data;
    }

    @Override
    public MyRecyclerViewAdapterForPlaylistView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);
        return new MyRecyclerViewAdapterForPlaylistView.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyRecyclerViewAdapterForPlaylistView.MyViewHolder holder, int position) {
        Information information = data.get(position);
        holder.title.setText(information.getTitle());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
