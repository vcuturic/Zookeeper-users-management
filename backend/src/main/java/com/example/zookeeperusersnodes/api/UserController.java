package com.example.zookeeperusersnodes.api;
import com.example.zookeeperusersnodes.annotation.LeaderOnly;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.services.interfaces.UserService;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import com.example.zookeeperusersnodes.zookeeper.DataStorage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ZooKeeperService zooKeeperService;

    public UserController(UserService userService, ZooKeeperService zooKeeperService) {
        this.userService = userService;
        this.zooKeeperService = zooKeeperService;
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<UserDTO>> getUsers() {
        return new ResponseEntity<>(DataStorage.getUserList(), HttpStatus.OK);
    }

    @LeaderOnly
    @PostMapping("/addUser")
    public ResponseEntity<ServerResponseDTO> addUser(@RequestBody UserDTO userDTO) {
        this.zooKeeperService.addUserZNode(userDTO, true);

        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully added user " + userDTO.getUsername());

        return new ResponseEntity<>(serverResponse, HttpStatus.OK);
    }

    @LeaderOnly
    @PostMapping("/removeUser")
    public ResponseEntity<ServerResponseDTO> removeUser(@RequestParam(name = "userRemoved") String username) {
        this.zooKeeperService.removeUserZNode(username);

        ServerResponseDTO serverResponse = new ServerResponseDTO("User " + username + " removed.");

        return new ResponseEntity<>(serverResponse, HttpStatus.OK);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestBody UserDTO userDTO) {
        if(userDTO.getUsername() == null || userDTO.getUsername().isEmpty())
            return ResponseEntity.badRequest().build();
        else {

            // if a user is disconnected, refresh it
            if(!this.userService.getUserActivity().containsKey(userDTO.getUsername())) {
                this.zooKeeperService.addUserZNode(userDTO, false);
                this.userService.getUserActivity().put(userDTO.getUsername(), System.currentTimeMillis());
            }

            return ResponseEntity.ok().build();
        }
    }

    @Scheduled(fixedRate = 100000)
    public void checkInactiveUsers() {
        long currentTime = System.currentTimeMillis();

        this.userService.getUserActivity().entrySet().removeIf(entry -> {
            boolean inactive = currentTime - entry.getValue() > 100000;
            if (inactive) {
                this.zooKeeperService.logoutUser(entry.getKey());
            }
            return inactive;
        });
    }
}
