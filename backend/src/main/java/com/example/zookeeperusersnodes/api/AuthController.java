package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.annotation.LeaderOnly;
import com.example.zookeeperusersnodes.bl.ZooKeeperBL;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ZooKeeperBL zooKeeperBL;

    public AuthController(ZooKeeperBL zooKeeperBL) {
        this.zooKeeperBL = zooKeeperBL;
    }
    @LeaderOnly
    @PostMapping("/login")
    public ResponseEntity<ServerResponseDTO> Login(
            @RequestBody UserDTO userDTO,
            @RequestParam(name = "userAdded", required = false, defaultValue = "false") boolean userAdded) {

        ServerResponseDTO serverResponse;

        this.zooKeeperBL.addZNode(userDTO.getUsername(), userAdded);

        if(userAdded)
            serverResponse = new ServerResponseDTO("Successfully added user " + userDTO.getUsername());
        else
            serverResponse = new ServerResponseDTO("Successfully logged in.");

        return new ResponseEntity<>(serverResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ServerResponseDTO> Logout(@CookieValue(value = "username", defaultValue = "ss") String username) {
        // TODO Remove user from /live_nodes

        System.out.println("Cookie Value: " + username);
        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully logged out.");

        return ResponseEntity.ok(serverResponse);
    }
}
