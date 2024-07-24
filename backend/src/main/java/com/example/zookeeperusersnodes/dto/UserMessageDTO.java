package com.example.zookeeperusersnodes.dto;

public class UserMessageDTO {
    private String username;
    private String message;

    public UserMessageDTO(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public UserMessageDTO() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
