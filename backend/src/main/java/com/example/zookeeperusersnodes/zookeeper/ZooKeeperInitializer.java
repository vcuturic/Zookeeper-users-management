package com.example.zookeeperusersnodes.zookeeper;

import org.apache.zookeeper.ZooKeeper;

public interface ZooKeeperInitializer {
    ZooKeeper getZooKeeperInstance();
}
