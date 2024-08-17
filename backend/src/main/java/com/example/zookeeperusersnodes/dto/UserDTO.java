package com.example.zookeeperusersnodes.dto;

import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {
    private String username;
    private String password;
    private boolean online;
    private String address;

    public UserDTO(String username, String password, boolean online, String address) {
        this.username = username;
        this.password = password;
        this.online = online;
        this.address = address;
    }

    public UserDTO(String username, boolean online, String address) {
        this.username = username;
        this.online = online;
        this.address = address;
    }

    public UserDTO(String username, String password, String address) {
        this.username = username;
        this.password = password;
        this.address = address;
    }

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserDTO(String username) {
        this.username = username;
    }

    public UserDTO() {

    }

    public static List<String> getUsernames(List<UserDTO> users) {
        return users.stream()
                .map(UserDTO::getUsername)
                .collect(Collectors.toList());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
