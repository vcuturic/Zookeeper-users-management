package com.example.zookeeperusersnodes.bl;
import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.example.zookeeperusersnodes.zookeeper.ZooKeeperInitializer;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.zookeeperusersnodes.constants.NodePaths.MESSAGING_PATH;

@Service
public class ZooKeeperBLImpl implements ZooKeeperBL {
    @Autowired
    private ZooKeeperInitializer zooKeeperInitializer;
    private final ZooKeeper zooKeeper;

    public ZooKeeperBLImpl(ZooKeeperInitializer zooKeeperInitializer) {
        this.zooKeeper = zooKeeperInitializer.getZooKeeperInstance();
    }

    @Override
    public List<NodeDTO> getZookeeperServerNodes() {
        List<NodeDTO> zNodes = new ArrayList<>();
        this.populateZookeeperNodes("/", zNodes);
        return zNodes;
    }

    @Override
    public List<String> getAllNodesChildren() {
        return ClusterInfo.getClusterInfo().getAllNodes();
    }

    @Override
    public List<String> getLiveNodesChildren() {
        return ClusterInfo.getClusterInfo().getLiveNodes();
    }

    @Override
    public String getLeaderInfo() {
        return ClusterInfo.getClusterInfo().getLeaderNode();
    }

    @Override
    public String getLeaderAddress() {
        return ClusterInfo.getClusterInfo().getLeaderAddress();
    }

    @Override
    public void addZNode(String username) {

        String newNodeName = "/" + username;

        try {
            if(zooKeeper.exists(NodePaths.ALL_NODES_PATH + newNodeName, false) == null) {
                // EPHEMERAL
                zooKeeper.create(NodePaths.LIVE_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                // PERSISTENT
                zooKeeper.create(NodePaths.ALL_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            else if(zooKeeper.exists(NodePaths.LIVE_NODES_PATH + newNodeName, false) == null) {
                // THEN, the node is back online
                zooKeeper.create(NodePaths.LIVE_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
        }
        catch (KeeperException | InterruptedException e) {
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void addMessageZNode(String username, String message) {
        String newNodeName = "/" + username;

        try {
            // If user already sent some messages we create only its children with new messages
            if(zooKeeper.exists(MESSAGING_PATH + newNodeName, false) == null) {
                zooKeeper.create(NodePaths.MESSAGING_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            zooKeeper.create(NodePaths.MESSAGING_PATH + newNodeName + "/message-", message.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        }
        catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void populateZookeeperNodes(String path, List<NodeDTO> parentsChildren) {
        List<String> children = null;

        try {
            children = zooKeeper.getChildren(path, false);
        } catch (KeeperException e) {
            // SESSION TIMEOUT EXCEPTION, NO NODE EXCEPTION
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<NodeDTO> newNodeChildren = new ArrayList<>();
        String fixedPath;

        if(children != null) {
            for (String child : children) {
                // WARNING! path will be "/" in the first instance
                fixedPath = path.equals("/") ? path + child : path + "/" + child;

                populateZookeeperNodes(fixedPath, newNodeChildren);
            }
        }

        // For ex. "/election/Node0000000000028"
        int lastIndex = path.lastIndexOf("/");
        String nodeName = path.substring(lastIndex + 1);

        // Node checked its children, populated the list if there were any
        NodeDTO newNode = new NodeDTO(nodeName, null, newNodeChildren);
        parentsChildren.add(newNode);
    }
}
