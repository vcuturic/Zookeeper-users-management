package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.services.interfaces.MessageService;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.services.interfaces.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final NotificationService notificationService;
    private final MessageService messageService;
    ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ws.instances}")
    private List<String> webSocketInstances;

    @Value("${server.port}")
    private int serverPort;

    public WebSocketServiceImpl(NotificationService notificationService, MessageService messageService) {
        this.notificationService = notificationService;
        this.messageService = messageService;
    }

    @Override
    public void broadcast(String expectServer, NodeDTO nodeDTO) {
        // expectServer - Address of the leader, no need to send updates to itself
        List<String> instancesWithoutLeader = new ArrayList<>(webSocketInstances);
        String portString = ":" + serverPort + "/";
        instancesWithoutLeader.removeIf(url -> url.contains(portString));

        for (String wsInstance : instancesWithoutLeader) {
            StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

            try {
                WebSocketSession webSocketSession = webSocketClient.execute(
                        new WebSocketHandler(this.notificationService, this.messageService), wsInstance).get();

                String jsonMessage = objectMapper.writeValueAsString(nodeDTO);
                webSocketSession.sendMessage(new TextMessage(jsonMessage));

                webSocketSession.close();
            }
            catch (InterruptedException | ExecutionException | IOException e) {
//                throw new RuntimeException(e);
                    // if some server is not connected, its crashing...
            }
        }
    }

    @Override
    public void broadcastMessage(String expectServer, UserMessageDTO userMessage) {
        // expectServer - Address of the leader, no need to send updates to itself
        List<String> instancesWithoutLeader = new ArrayList<>(webSocketInstances);
        String portString = ":" + serverPort + "/";
        instancesWithoutLeader.removeIf(url -> url.contains(portString));

        for (String wsInstance : instancesWithoutLeader) {
            StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

            try {
                WebSocketSession webSocketSession = webSocketClient.execute(
                        new WebSocketHandler(this.notificationService, this.messageService), wsInstance).get();

                String jsonMessage = objectMapper.writeValueAsString(userMessage);
                webSocketSession.sendMessage(new TextMessage(jsonMessage));

                webSocketSession.close();
            }
            catch (InterruptedException | ExecutionException | IOException e) {
//                throw new RuntimeException(e);
                // if some server is not connected, its crashing...
            }
        }
    }
}
