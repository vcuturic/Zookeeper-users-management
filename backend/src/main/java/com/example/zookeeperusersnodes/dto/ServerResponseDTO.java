package com.example.zookeeperusersnodes.dto;

public class ServerResponseDTO {
    private String message;

    public ServerResponseDTO(String message) {
        this.message = message;
    }

    public ServerResponseDTO() {

    }

    public String getMessage() {
        return message;
    }
}
