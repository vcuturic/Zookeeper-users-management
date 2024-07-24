package com.example.zookeeperusersnodes.api;
import com.example.zookeeperusersnodes.annotation.LeaderOnly;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController {
    private final ZooKeeperService zooKeeperService;

    public MessageController(ZooKeeperService zooKeeperService) {
        this.zooKeeperService = zooKeeperService;
    }

    @LeaderOnly
    @PostMapping("/receive")
    public ResponseEntity<ServerResponseDTO> receiveMessage(@RequestBody UserMessageDTO userMessageDTO) {
        this.zooKeeperService.addMessageZNode(userMessageDTO.getUsername(), userMessageDTO.getMessage());

        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully received message.");

        return ResponseEntity.ok(serverResponse);
    }
}
