package com.example.zookeeperusersnodes.services.interfaces;

public interface NotificationService {
    String OPERATION_DELETE = "delete";
    String OPERATION_CONNECT = "connected";
    String OPERATION_RECONNECT = "reconnected";
    String DESTINATION_ROUTE = "/topic/notifications";

    void nodeDeletedNotification(String deletedNode);
    void nodeDeletedNotification(String deletedNode, int nodeType);
    void nodeReconnectedNotification(String reconnectedNode);
    void nodeReconnectedNotification(String reconnectedNode, int nodeType);
    void nodeConnectedNotification(String addedNode);
    void nodeConnectedNotification(String addedNode, int nodeType);
}
