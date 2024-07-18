package com.example.zookeeperusersnodes.services.interfaces;

import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.dto.NodeDTO;

public interface WebSocketService {
    void broadcast(String expectServer, NodeDTO nodeDTO);
    void broadcast(String expectServer, NodeDTO nodeDTO, String nodeOperation);
}
