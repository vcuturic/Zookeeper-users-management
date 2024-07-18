package com.example.zookeeperusersnodes.zookeeper;

import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.services.interfaces.MessageService;
import com.example.zookeeperusersnodes.utils.CommonUtils;
import com.example.zookeeperusersnodes.zookeeper.interfaces.WatchersManager;
import com.example.zookeeperusersnodes.zookeeper.watchers.MessageWatcher;
import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Messenger implements Watcher {
    private final ZooKeeper zooKeeper;

    private final WatchersManager watcherManager;
    private final MessageService messageService;

    public Messenger(ZooKeeperInitializer zooKeeperInitializer, WatchersManager watcherManager, MessageService messageService) {
        this.zooKeeper = zooKeeperInitializer.getZooKeeperInstance();
        this.watcherManager = watcherManager;
        this.messageService = messageService;
    }

    public void addWatchersOnNodeChildren(String nodePath) throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren(nodePath, this);
        List<String> serverNodesAddresses = CommonUtils.getServerNodes(this.zooKeeper);
        String childPath;

        // Here the children are usernames (Pera, Mika, Zika) - We need watchers on them
        for (String child : children) {
            childPath = NodePaths.ALL_NODES_PATH + "/" + child;

            if(!watcherManager.hasWatcher(childPath) && !serverNodesAddresses.contains(child)) {
                watcherManager.addWatcher(childPath);
                zooKeeper.addWatch(childPath, new MessageWatcher(this.zooKeeper, this.messageService), AddWatchMode.PERSISTENT);
                System.out.println("Created watcher on: " + childPath);
            }
        }
    }

    @PostConstruct
    public void init() throws InterruptedException, KeeperException {
        zooKeeper.addWatch(NodePaths.ALL_NODES_PATH, this, AddWatchMode.PERSISTENT);
        
        // If there are already some children without watcher
        addWatchersOnNodeChildren(NodePaths.ALL_NODES_PATH);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getPath().equals(NodePaths.ALL_NODES_PATH)) {
            if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                try {
                    addWatchersOnNodeChildren(NodePaths.ALL_NODES_PATH);
                }
                catch (InterruptedException | KeeperException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
