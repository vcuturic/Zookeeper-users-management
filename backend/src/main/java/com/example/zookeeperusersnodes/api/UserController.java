package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.annotation.LeaderOnly;
import com.example.zookeeperusersnodes.bl.ZooKeeperBL;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/user")
public class UserController {

    private final Map<String, Long> userActivity = new ConcurrentHashMap<>();
    private final ZooKeeperBL zooKeeperBL;
    private boolean userDisconnected = true;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserController(ZooKeeperBL zooKeeperBL) {
        this.zooKeeperBL = zooKeeperBL;
    }

    @LeaderOnly
    @PostMapping("/removeUser")
    public ResponseEntity<ServerResponseDTO> removeUser(@RequestParam(name = "userRemoved") String username) {
        this.zooKeeperBL.removeZNode(username);

        ServerResponseDTO serverResponse = new ServerResponseDTO("User " + username + " removed.");

        return new ResponseEntity<>(serverResponse, HttpStatus.OK);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@CookieValue(value = "username", defaultValue = "error") String username) {
        if(username.equals("error"))
            return ResponseEntity.badRequest().build();
        else {
            userActivity.put(username, System.currentTimeMillis());

            if(userDisconnected) {
                this.zooKeeperBL.addZNode(username);
                userDisconnected = false;
            }

            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ServerResponseDTO> userLeft(@CookieValue(value = "username", defaultValue = "error") String username) {
        userActivity.remove(username);
        System.out.println("Removed user: " + username);

        ServerResponseDTO serverResponse = new ServerResponseDTO("Logging you out.");

        return ResponseEntity.ok(serverResponse);
    }

//     Periodically check for inactive users
    @Scheduled(fixedRate = 60000)
    public void checkInactiveUsers() {
        long currentTime = System.currentTimeMillis();

        userActivity.entrySet().removeIf(entry -> {
            boolean inactive = currentTime - entry.getValue() > 60000;
            if (inactive) {
                System.out.println("Removing inactive user: " + entry.getKey());
                userDisconnected = true;
                this.zooKeeperBL.removeZNodeFromLiveNodes(entry.getKey());
            }
            return inactive;
        });
    }
}
