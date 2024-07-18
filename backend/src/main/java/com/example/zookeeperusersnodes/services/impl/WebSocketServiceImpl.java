package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.services.interfaces.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final NotificationService notificationService;
    ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ws.instances}")
    private List<String> webSocketInstances;

    @Value("${server.port}")
    private int serverPort;

    public WebSocketServiceImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void broadcast(String expectServer, NodeDTO nodeDTO) {
        // expectServer - Address of the leader, no need to send updates to itself
        List<String> instancesWithoutLeader = new ArrayList<>(webSocketInstances);
        String portString = ":" + serverPort + "/";
        instancesWithoutLeader.removeIf(url -> url.contains(portString));

        //String serverUri = "ws://localhost:9090/ws-endpoint2"; // Replace with receiver!!

        for (String wsInstance : instancesWithoutLeader) {
            StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

            try {
                WebSocketSession webSocketSession = webSocketClient.execute(
                        new WebSocketHandler(this.notificationService), wsInstance).get();

//                NodeDTO nodeDTO = new NodeDTO("admin2", 2, false);
                String jsonMessage = objectMapper.writeValueAsString(nodeDTO);
                webSocketSession.sendMessage(new TextMessage(jsonMessage));

                // Close the WebSocket session
                webSocketSession.close();
            }
            catch (InterruptedException | ExecutionException | IOException e) {
//                throw new RuntimeException(e); // for now
            }
        }
    }

    @Override
    public void broadcast(String expectServer, NodeDTO nodeDTO, String nodeOperation) {

    }
}
