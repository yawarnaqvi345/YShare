package com.example.yshare.strucmodels;

import android.provider.MediaStore;

import java.util.Date;

public class Video implements  Comparable<Video> {
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MediaStore.Video.Thumbnails getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(MediaStore.Video.Thumbnails thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    String title;
    String path;
    MediaStore.Video.Thumbnails thumbnail;
    Date dateModified;

    @Override
    public int compareTo(Video o) {
        if (dateModified.after(o.getDateModified())){
            return -1;
        }
        else if (dateModified.before(o.getDateModified())) {
            return 1;
        }
        else {
            return 0;
        }

    }
}
