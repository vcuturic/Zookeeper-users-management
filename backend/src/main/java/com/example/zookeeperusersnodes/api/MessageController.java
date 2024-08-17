package com.example.zookeeperusersnodes.api;
import com.example.zookeeperusersnodes.annotation.LeaderOnly;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {
    private final ZooKeeperService zooKeeperService;

    public MessageController(ZooKeeperService zooKeeperService) {
        this.zooKeeperService = zooKeeperService;
    }

    @LeaderOnly
    @PostMapping("/receive")
    public ResponseEntity<ServerResponseDTO> receiveMessage(@RequestBody UserMessageDTO userMessageDTO, @RequestHeader("request-from") String requestFrom) {
        this.zooKeeperService.addMessageZNode(userMessageDTO);

        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully received message.");

        return ResponseEntity.ok(serverResponse);
    }
}
