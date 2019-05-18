package com.example.yshare.strucmodels;

public class TextMessage {
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    String text;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    String sender;
}
