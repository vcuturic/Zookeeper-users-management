package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
