package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.config.ZooKeeperInitializer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/zookeeper")
public class ZookeeperController {

    @Autowired
    private ZooKeeperInitializer zookeeperInitializer;

    /*@GetMapping("/")
    public String home() {
        return "{\"msg\": \"Hello World.\"}";
    }*/

    @GetMapping("/nodes")
    public List<String> zookeeperNodes() throws InterruptedException, KeeperException {
        ZooKeeper zooKeeper = zookeeperInitializer.getZooKeeperInstance();
        return zooKeeper.getChildren("/", false);
    }
}
