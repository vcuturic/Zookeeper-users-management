package com.example.zookeeperusersnodes.dto;

public class ServerResponseDTO {
    private final String message;

    public ServerResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
