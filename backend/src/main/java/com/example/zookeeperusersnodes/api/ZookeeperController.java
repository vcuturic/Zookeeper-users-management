package com.example.zookeeperusersnodes.api;
import com.example.zookeeperusersnodes.bl.ZooKeeperBL;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/zookeeper")
public class ZookeeperController {
    private final ZooKeeperBL zooKeeperBL;
    @Autowired
    private RestTemplate restTemplate;

    public ZookeeperController(ZooKeeperBL zooKeeperBL) {
        this.zooKeeperBL = zooKeeperBL;
    }
    @GetMapping("/servernodes")
    public List<NodeDTO> getZookeeperServerNodes() {
        return this.zooKeeperBL.getZookeeperServerNodes();
    }

    @GetMapping("/allnodes")
    public List<String> getAllNodesChildren() {
        return this.zooKeeperBL.getAllNodesChildren();
    }

    @GetMapping("/livenodes")
    public List<String> getLiveNodesChildren() {
        return this.zooKeeperBL.getLiveNodesChildren();
    }

    @GetMapping("/availability")
    public String checkServerAvailability() {
        return "{\"msg\": \"Server is available\"}";
    }

    // COMMUNICATION BETWEEN INSTANCES

    // SERVICE A
    @GetMapping("/data")
    public String getData() {
        return "Hello from Service A!";
    }

    // SERVICE B
    @GetMapping("/fetch-data")
    public String fetchData() {
        // Here we need zookeeper instance to get the first instance of /election
        // for that is the leader
        String leaderName = this.zooKeeperBL.getLeaderInfo();
        String leaderAddress = this.zooKeeperBL.getLeaderAddress();
        // Works!
        System.out.println("Communication: " + leaderName);
        System.out.println("Communication: " + leaderAddress);
        // TODO Get the leader address and send message to leader instead of hardcoded instance 1
        // When a user logs in from frontend he sends to its respective server a request to put
        // some user data into PersonInfo, PersonInfo is a list for now PersonInfo<>[];
        // so for ex. node can have 10 users (why not?)
        String url = "http://" + leaderAddress + "/zookeeper/data"; // URL of ServiceA
        return restTemplate.getForObject(url, String.class);
    }
}
