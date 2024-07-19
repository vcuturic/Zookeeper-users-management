package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.services.impl.WebSocketHandler;
import com.example.zookeeperusersnodes.services.impl.WebSocketInstanceImpl;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.services.interfaces.WebSocketInstance;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// Used for communication between leader and other zookeeper nodes
@RestController
@RequestMapping("comm")
public class CommunicationController {
    // This route is used when No-Leader nodes want to update (for ex. new no-leader node started) -
    // It needs to be up-to-date.
    @GetMapping("/clusterInfo")
    public ClusterInfo getClusterInfo() {
        return ClusterInfo.getClusterInfo();
    }
}
