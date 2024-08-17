package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.example.zookeeperusersnodes.zookeeper.DataStorage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/userInfo")
    public List<UserDTO> getUserInfo() {
        return DataStorage.getUserList();
    }
}
