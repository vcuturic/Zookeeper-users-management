package com.example.zookeeperusersnodes.zookeeper;

import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.realtime.interfaces.MessageService;
import com.example.zookeeperusersnodes.zookeeper.interfaces.WatchersManager;
import com.example.zookeeperusersnodes.zookeeper.watchers.MessageWatcher;
import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Autowired;
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
        String childPath;

        // Here the children are usernames (Pera, Mika, Zika) - We need watchers on them
        for (String child : children) {
            childPath = NodePaths.MESSAGING_PATH + "/" + child;

            if(!watcherManager.hasWatcher(childPath)) {
                watcherManager.addWatcher(childPath);
                zooKeeper.addWatch(childPath, new MessageWatcher(this.zooKeeper, this.messageService), AddWatchMode.PERSISTENT);
                System.out.println("Created watcher on: " + childPath);
            }
        }
    }

    @PostConstruct
    public void init() throws InterruptedException, KeeperException {
        zooKeeper.addWatch(NodePaths.MESSAGING_PATH, this, AddWatchMode.PERSISTENT);

        // If there are already some children without watcher
        addWatchersOnNodeChildren(NodePaths.MESSAGING_PATH);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getPath().equals(NodePaths.MESSAGING_PATH)) {
            if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                try {
                    addWatchersOnNodeChildren(NodePaths.MESSAGING_PATH);
                }
                catch (InterruptedException | KeeperException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
