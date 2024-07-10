package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.bl.ZooKeeperBL;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController {
    private final ZooKeeperBL zooKeeperBL;

    public MessageController(ZooKeeperBL zooKeeperBL) {
        this.zooKeeperBL = zooKeeperBL;
    }
    @PostMapping("/receive")
    public ResponseEntity<ServerResponseDTO> receiveMessage(@RequestBody UserMessageDTO userMessageDTO) {
        System.out.println(userMessageDTO.getUsername() + " " + userMessageDTO.getMessage());

        // TODO Samo dodati node /message/pero-1 npr
        this.zooKeeperBL.addMessageZNode(userMessageDTO.getUsername(), userMessageDTO.getMessage());

        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully received message.");

        return ResponseEntity.ok(serverResponse);
    }
}
