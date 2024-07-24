package com.example.zookeeperusersnodes.services.interfaces;

import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import org.apache.catalina.User;

public interface WebSocketService {
    void broadcast(String expectServer, NodeDTO nodeDTO);
    void broadcastMessage(String expectServer, UserMessageDTO userMessage);
}
