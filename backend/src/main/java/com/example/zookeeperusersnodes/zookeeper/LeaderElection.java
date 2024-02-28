package com.example.zookeeperusersnodes.zookeeper;

import com.example.zookeeperusersnodes.config.ZooKeeperInitializer;
import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@DependsOn("ZooKeeperInitializer")
public class LeaderElection implements Watcher {

    @Autowired
    private ZooKeeperInitializer zooKeeperInitializer;
    private String currentZNode;
    private static final String parentNode = "/election";

    @Override
    public void process(WatchedEvent watchedEvent) {

    }

    public void competeForLeader(String parentNode) throws InterruptedException, KeeperException {
        ZooKeeper zooKeeper = zooKeeperInitializer.getZooKeeperInstance();

        currentZNode = zooKeeper.create(parentNode + "/node", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        List<String> children = zooKeeper.getChildren(parentNode, false);
        children.sort(String::compareTo);

        if (currentZNode.equals(parentNode + "/" + children.getFirst())) {
            // I am the leader
            System.out.println("I am the leader!");
        } else {
            // I am not the leader
            System.out.println("I am not the leader.");
        }
    }

    @PostConstruct
    public void init6() throws InterruptedException, KeeperException {
        this.competeForLeader("/election");
    }
}
