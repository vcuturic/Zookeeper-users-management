package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.annotation.LeaderOnly;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.services.interfaces.UserService;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ZooKeeperService zooKeeperService;
    private final UserService userService;


    public AuthController(ZooKeeperService zooKeeperService, UserService userService) {
        this.zooKeeperService = zooKeeperService;
        this.userService = userService;
    }

    @LeaderOnly
    @PostMapping("/login")
    public ResponseEntity<ServerResponseDTO> Login(@RequestBody UserDTO userDTO) {
        this.zooKeeperService.addUserZNode(userDTO, false);
        System.out.println(userDTO.getAddress());

        this.userService.getUserActivity().put(userDTO.getUsername(), System.currentTimeMillis());

        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully logged in.");

        return new ResponseEntity<>(serverResponse, HttpStatus.OK);
    }

    @LeaderOnly
    @PostMapping("/logout")
    public ResponseEntity<ServerResponseDTO> Logout(@RequestBody String username) {
        if(username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ServerResponseDTO("User does not exist!"));
        }

//        this.zooKeeperService.removeZNodeFromLiveNodes(username);
        this.zooKeeperService.logoutUser(username);

        this.userService.userLeft(username);

        return ResponseEntity.ok(new ServerResponseDTO("Successfully logged out."));
    }
}
