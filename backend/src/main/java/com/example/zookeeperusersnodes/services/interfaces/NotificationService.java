package com.example.zookeeperusersnodes.services.interfaces;

public interface NotificationService {
    String DESTINATION_ROUTE = "/topic/notifications";

    void nodeDeletedNotification(String deletedNode);
    void nodeDeletedNotification(String deletedNode, int nodeType);
    void nodeDisconnectedNotification(String deletedNode);
    void nodeDisconnectedNotification(String deletedNode, int nodeType);
    void nodeReconnectedNotification(String reconnectedNode);
    void nodeReconnectedNotification(String reconnectedNode, int nodeType);
    void nodeConnectedOnlineNotification(String addedNode);
    void nodeConnectedOnlineNotification(String addedNode, int nodeType);
    void nodeConnectedOfflineNotification(String addedNode);
    void nodeConnectedOfflineNotification(String addedNode, int nodeType);
}
