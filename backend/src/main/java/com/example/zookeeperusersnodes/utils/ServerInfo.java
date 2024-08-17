package com.example.zookeeperusersnodes.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerInfo {
    @Value("${ws.instances}")
    private List<String> webSocketInstances;

    @Value("${server.port}")
    private int serverPort;


    public int getServerPort() {
        return serverPort;
    }

    public List<String> getWebSocketInstances() {
        return webSocketInstances;
    }
}
