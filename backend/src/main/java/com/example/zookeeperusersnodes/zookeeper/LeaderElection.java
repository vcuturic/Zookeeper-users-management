package com.example.zookeeperusersnodes.zookeeper;
import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class LeaderElection implements Watcher {

    @Value("${server.port}")
    private int serverPort;
    private static final String ELECTION_PATH = "/election";
    private static final String ALL_NODES_PATH = "/all_nodes";
    private static final String LIVE_NODES_PATH = "/live_nodes";
    private String currentZNode;
    private final ZooKeeper zooKeeper;

    public LeaderElection(ZooKeeperInitializer zooKeeperInitializer) {
        this.zooKeeper = zooKeeperInitializer.getZooKeeperInstance();
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
        else if(currentZNode.equals(ClusterInfo.getClusterInfo().getLeaderNode())) {
            if(watchedEvent.getPath().equals(ALL_NODES_PATH)) {
                System.out.println(ALL_NODES_PATH + ": Happenings");

            }
            else if(watchedEvent.getPath().equals(LIVE_NODES_PATH)) {
                System.out.println(LIVE_NODES_PATH + ": Happenings");
            }
        }
    }

    public void reevaluateLeadership() throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren(ELECTION_PATH, false);
        children.sort(String::compareTo);

        if (currentZNode.equals(ELECTION_PATH + "/" + children.getFirst())) {
            System.out.println("I am the leader! " + currentZNode);
            // CurrentZNode is the leader, add its Watcher() to /all_nodes and /live_nodes
            zooKeeper.addWatch(ALL_NODES_PATH, this, AddWatchMode.PERSISTENT);
            zooKeeper.addWatch(LIVE_NODES_PATH, this, AddWatchMode.PERSISTENT);
            ClusterInfo.getClusterInfo().setLeaderNode(currentZNode);
        }
        else {
            System.out.println("I am not the leader." + currentZNode);
        }

        // Whether the node is leader or not, add it to /all_nodes and /live_nodes
        this.addCurrentNode();
    }

    @PostConstruct
    public void init() throws InterruptedException, KeeperException {
        // Initialize LeaderElection Watcher
        zooKeeper.addWatch(ELECTION_PATH, this, AddWatchMode.PERSISTENT);

        // Compete for leadership
        currentZNode = zooKeeper.create(ELECTION_PATH + "/node", getServerInfo().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void addCurrentNode() {
        String newNodeName = "/" + getServerInfo();

        try {
            if(zooKeeper.exists(ALL_NODES_PATH + newNodeName, false) == null) {
                // PERSISTENT
                zooKeeper.create(ALL_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                // EPHEMERAL
                zooKeeper.create(LIVE_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
        }
        catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
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
