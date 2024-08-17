package com.example.zookeeperusersnodes.services.impl;
import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.services.interfaces.MessageService;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.zookeeper.DataStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Optional;

@Controller
public class WebSocketHandler extends TextWebSocketHandler {
    private final NotificationService notificationService;
    private final MessageService messageService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketHandler(NotificationService notificationService, MessageService messageService) {
        this.notificationService = notificationService;
        this.messageService = messageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
//        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        System.out.println("Received message: " + message.getPayload());

        JsonNode receivedNode = objectMapper.readTree(message.getPayload());

        if(receivedNode.has("name"))
            handleNodeMessage(objectMapper.treeToValue(receivedNode, NodeDTO.class));
        else
            handleUserMessage(objectMapper.treeToValue(receivedNode, UserMessageDTO.class));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
//        System.out.println("WebSocket connection closed: " + session.getId() + ", Reason: " + status.getReason());
    }

    void handleUserMessage(UserMessageDTO userMessage) {
        System.out.println("User message..");
        this.messageService.sendMessage(userMessage);
    }

    void handleNodeMessage(NodeDTO nodeDTO) {
        if(nodeDTO.status.equals(NodeOperations.OPERATION_CONNECT_ONLINE)) {
            this.notificationService.nodeConnectedOnlineNotification(nodeDTO.name, nodeDTO.type);

//            ClusterInfo.getClusterInfo().getAllNodes().add(nodeDTO.name);
//            ClusterInfo.getClusterInfo().getLiveNodes().add(nodeDTO.name);

            DataStorage.getUserList().add(new UserDTO(nodeDTO.name, true, null));
        }
        if(nodeDTO.status.equals(NodeOperations.OPERATION_CONNECT_OFFLINE)) {
            this.notificationService.nodeConnectedOfflineNotification(nodeDTO.name, nodeDTO.type);

//            ClusterInfo.getClusterInfo().getAllNodes().add(nodeDTO.name);
            DataStorage.getUserList().add(new UserDTO(nodeDTO.name, false, null));
        }
        if(nodeDTO.status.equals(NodeOperations.OPERATION_RECONNECT)) {
            this.notificationService.nodeReconnectedNotification(nodeDTO.name, nodeDTO.type);
//            ClusterInfo.getClusterInfo().getLiveNodes().add(nodeDTO.name);

            Optional<UserDTO> userOpt = DataStorage.getUserList().stream()
                    .filter(user -> user.getUsername().equals(nodeDTO.name))
                    .findFirst();

            userOpt.ifPresent(user -> {
                user.setOnline(true);
            });
        }
        if(nodeDTO.status.equals(NodeOperations.OPERATION_DISCONNECT)) {
            this.notificationService.nodeDisconnectedNotification(nodeDTO.name, nodeDTO.type);
//            ClusterInfo.getClusterInfo().getLiveNodes().remove(nodeDTO.name);

            Optional<UserDTO> userOpt = DataStorage.getUserList().stream()
                    .filter(user -> user.getUsername().equals(nodeDTO.name))
                    .findFirst();

            userOpt.ifPresent(user -> {
                user.setOnline(false);
            });
        }
        if(nodeDTO.status.equals(NodeOperations.OPERATION_DELETE)) {
            this.notificationService.nodeDeletedNotification(nodeDTO.name);

//            ClusterInfo.getClusterInfo().getAllNodes().remove(nodeDTO.name);
//            ClusterInfo.getClusterInfo().getLiveNodes().remove(nodeDTO.name);

            DataStorage.getUserList().removeIf(user -> user.getUsername().equals(nodeDTO.name));
        }
    }
}
