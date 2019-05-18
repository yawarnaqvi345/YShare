package com.example.yshare.strucmodels;

import android.provider.MediaStore;

import java.util.Date;

public class Image implements Comparable<Image> {
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MediaStore.Images.Thumbnails getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(MediaStore.Images.Thumbnails thumbnail) {
        this.thumbnail = thumbnail;
    }

    String title;
    Date lastModified;
    String path;
    MediaStore.Images.Thumbnails thumbnail;


    @Override
    public int compareTo(Image o) {
        if (lastModified.after(o.getLastModified())){
            return -1;
        }
        else if (lastModified.before(o.getLastModified())) {
            return 1;
        }
        else {
            return 0;
        }

    }
}
