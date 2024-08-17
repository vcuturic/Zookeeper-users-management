package com.example.zookeeperusersnodes.zookeeper;

import com.example.zookeeperusersnodes.constants.NodePaths;
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
        if (zooKeeper.exists(NodePaths.ALL_NODES_PATH, false) == null)
            zooKeeper.create(NodePaths.ALL_NODES_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if (zooKeeper.exists(NodePaths.LIVE_NODES_PATH, false) == null)
            zooKeeper.create(NodePaths.LIVE_NODES_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if (zooKeeper.exists(NodePaths.ELECTION_PATH, false) == null)
            zooKeeper.create(NodePaths.ELECTION_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if (zooKeeper.exists(NodePaths.USERS_PATH, false) == null)
            zooKeeper.create(NodePaths.USERS_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Override
    public ZooKeeper getZooKeeperInstance() {
        return zooKeeper;
    }
}
