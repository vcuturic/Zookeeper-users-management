package com.example.zookeeperusersnodes.services.impl;
import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.models.Message;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public NotificationServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public synchronized void nodeReconnectedNotification(String reconnectedNode) {
        Message msg = new Message(NodeOperations.OPERATION_RECONNECT, new NodeDTO(reconnectedNode, null, null));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeReconnectedNotification(String reconnectedNode, int nodeType) {
        Message msg = new Message(NodeOperations.OPERATION_RECONNECT, new NodeDTO(reconnectedNode, nodeType, true));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeConnectedOnlineNotification(String addedNode) {
        Message msg = new Message(NodeOperations.OPERATION_CONNECT_ONLINE, new NodeDTO(addedNode, null, null));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeConnectedOnlineNotification(String addedNode, int nodeType) {
        Message msg = new Message(NodeOperations.OPERATION_CONNECT_ONLINE, new NodeDTO(addedNode, nodeType, true));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeConnectedOfflineNotification(String addedNode) {
        Message msg = new Message(NodeOperations.OPERATION_CONNECT_OFFLINE, new NodeDTO(addedNode, null, null));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeConnectedOfflineNotification(String addedNode, int nodeType) {
        Message msg = new Message(NodeOperations.OPERATION_CONNECT_OFFLINE, new NodeDTO(addedNode, nodeType, true));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeDeletedNotification(String deletedNode) {
        Message msg = new Message(NodeOperations.OPERATION_DELETE, new NodeDTO(deletedNode, NodeOperations.OPERATION_DELETE));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeDeletedNotification(String deletedNode, int nodeType) {
        Message msg = new Message(NodeOperations.OPERATION_DELETE, new NodeDTO(deletedNode, nodeType, false));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeDisconnectedNotification(String deletedNode) {
        Message msg = new Message(NodeOperations.OPERATION_DISCONNECT, new NodeDTO(deletedNode, null, null, false));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public synchronized void nodeDisconnectedNotification(String deletedNode, int nodeType) {
        Message msg = new Message(NodeOperations.OPERATION_DISCONNECT, new NodeDTO(deletedNode, nodeType, false));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }
}
