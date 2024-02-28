package com.example.zookeeperusersnodes.config;

import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component("ZooKeeperInitializer")
public class ZooKeeperInitializer {
    private final static String connectionString = "localhost:2181";
    private final static int sessionTimeout = 3000;
    private ZooKeeper zooKeeper;
    private String currentZNode;
    private final static String parentNode = "/election";

    @PostConstruct
    private void init() throws IOException, InterruptedException, KeeperException {
        zooKeeper = new ZooKeeper(connectionString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // Watcher logic
            }
        });

        if (zooKeeper.exists("/all_nodes", false) == null)
            zooKeeper.create("/all_nodes", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if (zooKeeper.exists("/live_nodes", false) == null)
            zooKeeper.create("/live_nodes", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public ZooKeeper getZooKeeperInstance() {
        return zooKeeper;
    }
}
