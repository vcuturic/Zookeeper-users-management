package com.example.zookeeperusersnodes.services.interfaces;

import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import org.apache.catalina.User;

import java.util.List;

public interface WebSocketService {
    void broadcast(String expectServer, NodeDTO nodeDTO);
    void broadcastMessage(String expectServer, UserMessageDTO userMessage);
    void broadcastMessage(List<String> expectServers, UserMessageDTO userMessage);
    void broadcastMessageTo(List<String> toServers, UserMessageDTO userMessage);
}
