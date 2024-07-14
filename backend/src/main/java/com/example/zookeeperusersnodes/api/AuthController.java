package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.bl.ZooKeeperBL;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ZooKeeperBL zooKeeperBL;

    public AuthController(ZooKeeperBL zooKeeperBL) {
        this.zooKeeperBL = zooKeeperBL;
    }
    @PostMapping("/login")
    public ResponseEntity<ServerResponseDTO> Login(@RequestBody UserDTO userDTO) {
        this.zooKeeperBL.addZNode(userDTO.getUsername());

        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully logged in.");

        return ResponseEntity.ok(serverResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ServerResponseDTO> Logout(@CookieValue(value = "username", defaultValue = "ss") String username) {
        // TODO Remove user from /live_nodes

        System.out.println("Cookie Value: " + username);
        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully logged out.");

        return ResponseEntity.ok(serverResponse);
    }
}
