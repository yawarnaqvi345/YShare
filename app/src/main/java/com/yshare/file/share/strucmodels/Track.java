package com.yshare.file.share.strucmodels;

import java.util.Date;

public class Track implements Comparable<Track>{
    String title;
    String album;
    String artist;
    String length;
    String size;
    Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    String path;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public int compareTo(Track o) {
        if (date.after(o.getDate())){
            return -1;
        }
        else if (date.before(o.getDate())) {
            return 1;
        }
        else {
            return 0;
        }

    }
}