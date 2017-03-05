package com.example.asif.youtubemp3downloader;

/**
 * Created by ASIF on 3/5/2017.
 */

public class Information {

    private String title, id;

    public Information() {

    }

    public Information(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
