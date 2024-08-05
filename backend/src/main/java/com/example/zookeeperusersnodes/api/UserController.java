package com.example.zookeeperusersnodes.api;
import com.example.zookeeperusersnodes.annotation.LeaderOnly;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.services.interfaces.UserService;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ZooKeeperService zooKeeperService;
    private boolean userDisconnected = true;

    public UserController(UserService userService, ZooKeeperService zooKeeperService) {
        this.userService = userService;
        this.zooKeeperService = zooKeeperService;
    }

    @LeaderOnly
    @PostMapping("/addUser")
    public ResponseEntity<ServerResponseDTO> addUser(@RequestBody UserDTO userDTO) {
        this.zooKeeperService.addZNode(userDTO.getUsername(), true);

        ServerResponseDTO serverResponse = new ServerResponseDTO("Successfully added user " + userDTO.getUsername());

        return new ResponseEntity<>(serverResponse, HttpStatus.OK);
    }

    @LeaderOnly
    @PostMapping("/removeUser")
    public ResponseEntity<ServerResponseDTO> removeUser(@RequestParam(name = "userRemoved") String username) {
        this.zooKeeperService.removeZNode(username);

        ServerResponseDTO serverResponse = new ServerResponseDTO("User " + username + " removed.");

        return new ResponseEntity<>(serverResponse, HttpStatus.OK);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@CookieValue(value = "username", defaultValue = "error") String username) {
        if(username.equals("error"))
            return ResponseEntity.badRequest().build();
        else {
            this.userService.getUserActivity().put(username, System.currentTimeMillis());

            if(userDisconnected) {
                this.zooKeeperService.addZNode(username);
                userDisconnected = false;
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
                System.out.println("Removing inactive user: " + entry.getKey());
                userDisconnected = true;
                this.zooKeeperService.removeZNodeFromLiveNodes(entry.getKey());
            }
            return inactive;
        });
    }
}
