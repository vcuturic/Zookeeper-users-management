package com.example.zookeeperusersnodes.services.impl;
import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CompletableFuture;

@Controller
public class WebSocketHandler extends TextWebSocketHandler {
//    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    private NotificationService notificationService;

    public WebSocketHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        System.out.println("WebSocket connection established: " + session.getId());
//        session.sendMessage(new TextMessage("Connection established"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        System.out.println("Received message: " + message.getPayload());

        NodeDTO nodeDTO = new ObjectMapper().readValue(message.getPayload(), NodeDTO.class);

        if(nodeDTO.status.equals(NodeOperations.OPERATION_CONNECT_ONLINE)) {
            this.notificationService.nodeConnectedOnlineNotification(nodeDTO.name, nodeDTO.type);

            ClusterInfo.getClusterInfo().getAllNodes().add(nodeDTO.name);
            ClusterInfo.getClusterInfo().getLiveNodes().add(nodeDTO.name);
        }
        if(nodeDTO.status.equals(NodeOperations.OPERATION_CONNECT_OFFLINE)) {
            this.notificationService.nodeConnectedOfflineNotification(nodeDTO.name, nodeDTO.type);

            ClusterInfo.getClusterInfo().getAllNodes().add(nodeDTO.name);
        }
        if(nodeDTO.status.equals(NodeOperations.OPERATION_RECONNECT)) {
            this.notificationService.nodeReconnectedNotification(nodeDTO.name, nodeDTO.type);
            ClusterInfo.getClusterInfo().getLiveNodes().add(nodeDTO.name);
        }
        if(nodeDTO.status.equals(NodeOperations.OPERATION_DISCONNECT)) {
            this.notificationService.nodeDisconnectedNotification(nodeDTO.name, nodeDTO.type);
            ClusterInfo.getClusterInfo().getLiveNodes().remove(nodeDTO.name);
        }
        if(nodeDTO.status.equals(NodeOperations.OPERATION_DELETE)) {
            this.notificationService.nodeDeletedNotification(nodeDTO.name, nodeDTO.type);

            ClusterInfo.getClusterInfo().getAllNodes().remove(nodeDTO.name);
            ClusterInfo.getClusterInfo().getLiveNodes().remove(nodeDTO.name);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        System.out.println("WebSocket connection closed: " + session.getId() + ", Reason: " + status.getReason());
    }
}
