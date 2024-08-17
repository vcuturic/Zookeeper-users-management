package com.example.zookeeperusersnodes.dto;

public class UserMessageDTO {
    private String from;
    private String to;
    private String text;
    private boolean read = false;

    public UserMessageDTO(String from, String to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
    }

    public UserMessageDTO(String to, String text) {
        this.to = to;
        this.text = text;
    }

    public UserMessageDTO() {

    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean sent) {
        this.read = sent;
    }
}
