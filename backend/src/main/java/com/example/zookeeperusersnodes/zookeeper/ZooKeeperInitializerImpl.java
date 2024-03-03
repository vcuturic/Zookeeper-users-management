package com.example.zookeeperusersnodes.zookeeper;

import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("ZooKeeperInitializer")
public class ZooKeeperInitializerImpl implements ZooKeeperInitializer{
    private final static String connectionString = "localhost:2181";
    private final static int sessionTimeout = 3000;
    private ZooKeeper zooKeeper;

    @PostConstruct
    private void init() throws IOException, InterruptedException, KeeperException {
        zooKeeper = new ZooKeeper(connectionString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // Watcher logic
            }
        });

        this.createInitialNodes();
    }

    private void createInitialNodes() throws InterruptedException, KeeperException {
        if (zooKeeper.exists("/all_nodes", false) == null)
            zooKeeper.create("/all_nodes", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if (zooKeeper.exists("/live_nodes", false) == null)
            zooKeeper.create("/live_nodes", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if (zooKeeper.exists("/election", false) == null)
            zooKeeper.create("/election", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Override
    public ZooKeeper getZooKeeperInstance() {
        return zooKeeper;
    }
}
