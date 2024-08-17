package com.example.zookeeperusersnodes.zookeeper;

import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.services.interfaces.MessageService;
import com.example.zookeeperusersnodes.services.interfaces.WebSocketService;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import com.example.zookeeperusersnodes.utils.CommonUtils;
import com.example.zookeeperusersnodes.zookeeper.interfaces.WatchersManager;
import com.example.zookeeperusersnodes.zookeeper.watchers.MessageWatcher;
import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class Messenger implements Watcher {
    private final ZooKeeper zooKeeper;
    private final WatchersManager watcherManager;
    private final MessageService messageService;

    private final ZooKeeperService zooKeeperService;
    private final WebSocketService webSocketService;
    private CommonUtils commonUtils;

    public Messenger(ZooKeeper zooKeeper, WatchersManager watcherManager, MessageService messageService, ZooKeeperService zooKeeperService, WebSocketService webSocketService, CommonUtils commonUtils) {
        this.zooKeeper = zooKeeper;
        this.watcherManager = watcherManager;
        this.messageService = messageService;
        this.zooKeeperService = zooKeeperService;
        this.webSocketService = webSocketService;
        this.commonUtils = commonUtils;
    }

    public void addWatchersOnNodeChildren(String nodePath) throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren(nodePath, this);
        List<String> serverNodesAddresses =  zooKeeperService.getServerNodesFromAllNodes(children);
        String childPath;

        // Here the children are usernames (Pera, Mika, Zika) - We need watchers on them
        for (String child : children) {
            childPath = nodePath + "/" + child;

            if(!watcherManager.hasMessageWatcher(childPath) && !serverNodesAddresses.contains(child)) {
                watcherManager.addMessageWatcher(childPath);
                zooKeeper.addWatch(childPath, new MessageWatcher(this.zooKeeper, this.messageService, webSocketService, this.zooKeeperService, commonUtils), AddWatchMode.PERSISTENT);
                System.out.println("Created message watcher on: " + childPath);
            }
        }
    }

    public void init() throws InterruptedException, KeeperException {
        if (zooKeeperService.isThisNodeLeader(this.zooKeeperService.getCurrentZNode())) {

            zooKeeper.addWatch(NodePaths.USERS_PATH, this, AddWatchMode.PERSISTENT);

            // If there are already some children without watcher
            addWatchersOnNodeChildren(NodePaths.USERS_PATH);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getPath().equals(NodePaths.USERS_PATH)) {
            if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                try {
                    addWatchersOnNodeChildren(NodePaths.USERS_PATH);
                }
                catch (InterruptedException | KeeperException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
