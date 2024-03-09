package com.example.zookeeperusersnodes.zookeeper;
import com.example.zookeeperusersnodes.realtime.NotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Component
public class LeaderElection implements Watcher {

    @Value("${server.port}")
    private int serverPort;
    private static final String ELECTION_PATH = "/election";
    private static final String ALL_NODES_PATH = "/all_nodes";
    private static final String LIVE_NODES_PATH = "/live_nodes";
    private String currentZNode;
    private final ZooKeeper zooKeeper;
    private final NotificationService notificationService;

    public LeaderElection(ZooKeeperInitializer zooKeeperInitializer, NotificationService notificationService) {
        this.zooKeeper = zooKeeperInitializer.getZooKeeperInstance();
        this.notificationService = notificationService;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getPath().equals(ELECTION_PATH)) {
            if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                System.out.println("Node " +  watchedEvent.getType() + " event | " + watchedEvent.getPath());
                try {
                    this.reevaluateLeadership();
                }
                catch (InterruptedException | KeeperException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if(currentZNode.equals(ClusterInfo.getClusterInfo().getLeaderNode())) {
            if(watchedEvent.getPath().equals(ALL_NODES_PATH)) {
                if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                    try {
                        List<String> children = zooKeeper.getChildren(ALL_NODES_PATH, null);
                        ClusterInfo.getClusterInfo().getAllNodes().clear();
                        ClusterInfo.getClusterInfo().getAllNodes().addAll(children);

                    }
                    catch (KeeperException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(ALL_NODES_PATH + ": " + ClusterInfo.getClusterInfo().getAllNodes());
            }
            if(watchedEvent.getPath().equals(LIVE_NODES_PATH)) {
                if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                    try {
                        List<String> children = zooKeeper.getChildren(LIVE_NODES_PATH, null);
                        this.manageLiveChildren(children);

                    }
                    catch (KeeperException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(LIVE_NODES_PATH + ": " + ClusterInfo.getClusterInfo().getLiveNodes());
                }
            }
        }
    }

    public void getLatestClusterInfo() {
        try {
            // UPDATE all nodes nad live nodes.
            List<String> allNodes = zooKeeper.getChildren(ALL_NODES_PATH, false);
            List<String> liveNodes = zooKeeper.getChildren(LIVE_NODES_PATH, false);
            ClusterInfo.getClusterInfo().getAllNodes().clear();
            ClusterInfo.getClusterInfo().getAllNodes().addAll(allNodes);
            ClusterInfo.getClusterInfo().getLiveNodes().clear();
            ClusterInfo.getClusterInfo().getLiveNodes().addAll(liveNodes);
        }
        catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void reevaluateLeadership() throws InterruptedException, KeeperException {
        // TODO reelection to be only called when an leader disconnects
        List<String> children = zooKeeper.getChildren(ELECTION_PATH, false);

        if(children.isEmpty()) {
            // No more leaders, server shutting down
            System.out.println("Shutting down...");
            this.onServerDisconnect();
        }
        else {
            children.sort(String::compareTo);

            if (currentZNode.equals(ELECTION_PATH + "/" + children.getFirst())) {
                System.out.println("I am the leader! " + currentZNode);

                this.getLatestClusterInfo();
                // CurrentZNode is the leader, add its Watcher() to /all_nodes and /live_nodes
                zooKeeper.addWatch(ALL_NODES_PATH, this, AddWatchMode.PERSISTENT);
                zooKeeper.addWatch(LIVE_NODES_PATH, this, AddWatchMode.PERSISTENT);
                ClusterInfo.getClusterInfo().setLeaderNode(currentZNode);
            } else {
                System.out.println("I am not the leader." + currentZNode);
                ClusterInfo.getClusterInfo().setLeaderNode(ELECTION_PATH + "/" + children.getFirst());
            }

            // Whether the node is leader or not, add it to /all_nodes and /live_nodes
            this.addCurrentNode();
        }
    }

    @PostConstruct
    public void init() throws InterruptedException, KeeperException {
        // Initialize LeaderElection Watcher
        zooKeeper.addWatch(ELECTION_PATH, this, AddWatchMode.PERSISTENT);

        // Compete for leadership
        currentZNode = zooKeeper.create(ELECTION_PATH + "/node", getServerInfo().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    @PreDestroy
    public void onServerDisconnect() {
        if(currentZNode.equals(ClusterInfo.getClusterInfo().getLeaderNode())) {
            try {
                List<String> leaderCandidates = zooKeeper.getChildren(ELECTION_PATH, null);

                if(leaderCandidates.size() == 1) {
                    List<String> allNodesChildren = zooKeeper.getChildren(ALL_NODES_PATH, null);

                    for (String child : allNodesChildren) {
                        zooKeeper.delete(ALL_NODES_PATH + "/" + child, -1);
                    }
                }
            }
            catch (KeeperException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void manageLiveChildren(List<String> children) {
        Set<String> currentChildren = new HashSet<>(children);
        Set<String> previousChildren = new HashSet<>(ClusterInfo.getClusterInfo().getLiveNodes());

        if(ClusterInfo.getClusterInfo().getLiveNodes().size() < children.size()) {
            // Addition occurred
            currentChildren.removeAll(previousChildren);

            Iterator<String> iterator = currentChildren.iterator();
            String addedNode = iterator.hasNext() ? iterator.next() : null;

            if(ClusterInfo.getClusterInfo().getAllNodes().contains(addedNode)) {
                this.notificationService.nodeReconnectedNotification(addedNode);
            }
            else {
                this.notificationService.nodeConnectedNotification(addedNode);
            }

            ClusterInfo.getClusterInfo().getLiveNodes().clear();
            ClusterInfo.getClusterInfo().getLiveNodes().addAll(children);
        }
        else {
            // Deletion occurred
            previousChildren.removeAll(currentChildren);

            Iterator<String> iterator = previousChildren.iterator();
            String deletedNode = iterator.hasNext() ? iterator.next() : null;

            this.notificationService.nodeDeletedNotification(deletedNode);

            ClusterInfo.getClusterInfo().setLiveNodes(children);
        }
    }

    public void addCurrentNode() {
        String newNodeName = "/" + getServerInfo();

        try {
            if(zooKeeper.exists(ALL_NODES_PATH + newNodeName, false) == null) {
                // EPHEMERAL
                zooKeeper.create(LIVE_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                // PERSISTENT
                zooKeeper.create(ALL_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            else if(zooKeeper.exists(LIVE_NODES_PATH + newNodeName, false) == null) {
                // THEN, the node is back online
                zooKeeper.create(LIVE_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
            else {
                // The current node is in all_nodes and in /live_nodes, probably reelection
                System.out.println("Hello");
            }
//            this.getLatestClusterInfo();
        }
        catch (KeeperException | InterruptedException e) {
//            throw new RuntimeException(e);
        }
    }

    public String getServerInfo() {

        String address;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        return address != null ? address + ":" + serverPort : null;
    }
}
