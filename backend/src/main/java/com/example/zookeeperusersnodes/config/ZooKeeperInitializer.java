package com.example.zookeeperusersnodes.config;

import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ZooKeeperInitializer {
    private final static String connectionString = "localhost:2181";
    private final static int sessionTimeout = 3000;

    private ZooKeeper zooKeeper;

    @PostConstruct
    private void init() throws IOException {
        zooKeeper = new ZooKeeper(connectionString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // Watcher logic
            }
        });
    }

    public ZooKeeper getZooKeeperInstance() {
        return zooKeeper;
    }
}
