package com.example.asif.youtubemp3downloader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ASIF on 3/4/2017.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Information> data;

    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, id;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            id = (TextView) view.findViewById(R.id.id);
        }
    }

    MyRecyclerViewAdapter(Context context, ArrayList<Information> information) {
        this.data = information;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Information information = data.get(position);
        holder.title.setText(information.getTitle());
        holder.id.setText(information.getId());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

}
