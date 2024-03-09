package com.example.zookeeperusersnodes.realtime;

public interface NotificationService {
    String OPERATION_DELETE = "delete";
    String OPERATION_CONNECT = "connected";
    String OPERATION_RECONNECT = "reconnected";
    String DESTINATION_ROUTE = "/topic/notifications";

    void nodeDeletedNotification(String deletedNode);
    void nodeReconnectedNotification(String reconnectedNode);
    void nodeConnectedNotification(String addedNode);
}
