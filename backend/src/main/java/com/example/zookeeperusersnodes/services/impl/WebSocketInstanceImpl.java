package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.services.interfaces.WebSocketInstance;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

//@Service
public class WebSocketInstanceImpl {
    private WebSocketClient client;
    private Map<String, WebSocketSession> sessions = new HashMap<>();

    @Value("${ws.instances}")
    private String[] webSocketInstances;

    @PostConstruct
    public void init() {
        client = new StandardWebSocketClient();
        connectToBackends();
    }

    private void connectToBackends() {
        for (String backendInstance : webSocketInstances) {

            WebSocketSession session = null;
            try {
                session = client.execute(new TextWebSocketHandler() {}, String.valueOf(URI.create(backendInstance))).get();
                sessions.put(backendInstance, session);
            }
            catch (InterruptedException | ExecutionException e) {
//                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessageToInstance(String instanceUrl, String message) {
        WebSocketSession session = sessions.get(instanceUrl);
        if (session != null && session.isOpen()) {

            try {
                session.sendMessage(new TextMessage(message));
            }
            catch (IOException e) {
//                throw new RuntimeException(e);
            }

        }
    }
}
