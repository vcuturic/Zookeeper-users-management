package com.example.zookeeperusersnodes.realtime;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public NotificationServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void nodeConnectedNotification(String addedNode) {
        Message msg = new Message(OPERATION_CONNECT, new NodeDTO(addedNode, null, null));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }
    @Override
    public void nodeReconnectedNotification(String reconnectedNode) {
        Message msg = new Message(OPERATION_RECONNECT, new NodeDTO(reconnectedNode, null, null));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }

    @Override
    public void nodeDeletedNotification(String deletedNode) {
        Message msg = new Message(OPERATION_DELETE, new NodeDTO(deletedNode, null, null, false));
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, msg);
    }
}
