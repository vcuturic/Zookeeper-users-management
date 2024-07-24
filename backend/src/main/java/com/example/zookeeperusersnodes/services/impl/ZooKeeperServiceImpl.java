package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.constants.NodeTypes;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import com.example.zookeeperusersnodes.utils.IPAddressChecker;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.example.zookeeperusersnodes.zookeeper.ZooKeeperInitializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.zookeeperusersnodes.constants.NodePaths.ELECTION_PATH;

@Service
public class ZooKeeperServiceImpl implements ZooKeeperService {
    private String currentZNode;

    @Autowired
    private ZooKeeperInitializer zooKeeperInitializer;
    private final ZooKeeper zooKeeper;

    public ZooKeeperServiceImpl(ZooKeeperInitializer zooKeeperInitializer) {
        this.zooKeeper = zooKeeperInitializer.getZooKeeperInstance();
    }

    @Override
    public String getCurrentZNode() {
        return this.currentZNode;
    }

    @Override
    public void setCurrentZNode(String currentZNode) {
        this.currentZNode = currentZNode;
    }

    @Override
    public List<NodeDTO> getZookeeperServerNodes() {
        List<NodeDTO> zNodes = new ArrayList<>();
        this.populateZookeeperNodes("/", zNodes);
        return zNodes;
    }

    @Override
    public List<NodeDTO> getAllNodesChildren() {
        try {
            List<String> children = zooKeeper.getChildren(NodePaths.ALL_NODES_PATH, false);

//            ELECTION_PATH Nodes have data, and they only are server nodes
            List<String> serverNodes = zooKeeper.getChildren(NodePaths.ELECTION_PATH, false);

            List<NodeDTO> childrenDTO = new ArrayList<>();

            for (String serverNode : serverNodes) {
                String serverNodePath = ELECTION_PATH + "/" + serverNode;
                byte[] data = zooKeeper.getData(serverNodePath, false, null);

                if (data != null && data.length > 0) {
                    childrenDTO.add(new NodeDTO(new String(data), NodeTypes.ZNODE_TYPE_SERVER, false));
                }
            }

            for (String child : children) {

                if(!containsName(childrenDTO, child)) {
                    childrenDTO.add(new NodeDTO(child, NodeTypes.ZNODE_TYPE_USER, false));
                }
            }

            return childrenDTO;
        }
        catch (KeeperException | InterruptedException e) {
//            throw new RuntimeException(e);
        }

        return null;
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
    public boolean isThisNodeLeader(String nodeName) {
        return ClusterInfo.getClusterInfo().getLeaderNode().equals(nodeName);
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
                // PERSISTENT
                zooKeeper.create(NodePaths.ALL_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                // EPHEMERAL
                zooKeeper.create(NodePaths.LIVE_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
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
    public void addZNode(String username, boolean userAdded) {
        String newNodeName = "/" + username;

        try {
            if(zooKeeper.exists(NodePaths.ALL_NODES_PATH + newNodeName, false) == null) {
                // PERSISTENT
                zooKeeper.create(NodePaths.ALL_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                // EPHEMERAL
                if(!userAdded)
                    zooKeeper.create(NodePaths.LIVE_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
            else if(zooKeeper.exists(NodePaths.LIVE_NODES_PATH + newNodeName, false) == null && !userAdded) {
                // THEN, the node is back online
                zooKeeper.create(NodePaths.LIVE_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
        }
        catch (KeeperException | InterruptedException e) {
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeZNodeFromLiveNodes(String username) {
        String newNodeName = "/" + username;

        try {
            if(zooKeeper.exists(NodePaths.LIVE_NODES_PATH + newNodeName, false) != null) {
                zooKeeper.delete(NodePaths.LIVE_NODES_PATH + newNodeName, -1);
            }
        } catch (KeeperException | InterruptedException e) {
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeZNode(String username) {
        String newNodeName = "/" + username;

        try {
            if(zooKeeper.exists(NodePaths.ALL_NODES_PATH + newNodeName, false) != null) {
                zooKeeper.delete(NodePaths.ALL_NODES_PATH + newNodeName, -1);
            }

            this.removeZNodeFromLiveNodes(username);
        }
        catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addMessageZNode(String username, String message) {
        String newNodeName = "/" + username;

        try {
            // If user already sent some messages we create only its children with new messages
            if(zooKeeper.exists(NodePaths.ALL_NODES_PATH + newNodeName, false) == null) {
                zooKeeper.create(NodePaths.ALL_NODES_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            zooKeeper.create(NodePaths.ALL_NODES_PATH + newNodeName + "/message-", message.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
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

    public List<String> getServerNodesFromAllNodes(List<String> allNodes) {
        List<String> allNodesCopy = new ArrayList<>(allNodes);
        List<String> serverNodesAddresses = new ArrayList<>();

        for (String zNode : allNodesCopy) {
            if(IPAddressChecker.isIPAddress(zNode)) {
                serverNodesAddresses.add(zNode);
            }
        }

        return serverNodesAddresses;
    }

    public static boolean containsName(List<NodeDTO> list, String name) {
        return list.stream().anyMatch(dto -> dto.name.equals(name));
    }
}
