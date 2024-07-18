package com.example.zookeeperusersnodes.services.interfaces;

public interface WebSocketInstance {


    void connectToBackends();
    void sendMessageToInstance(String instanceUrl, String message);
}
