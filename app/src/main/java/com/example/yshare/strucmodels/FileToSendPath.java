package com.example.yshare.strucmodels;

public class FileToSendPath {


    String name;
    String path;
    String type;
    int progress;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getProgress() {
        return progress;
    }
    public void setProgress(int progress) {
        this.progress = progress;
    }
    public FileToSendPath(){
        path=null;
        type=null;
        progress=0;
        name=null;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

}
