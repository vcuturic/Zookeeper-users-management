package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.constants.NodeTypes;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import com.example.zookeeperusersnodes.utils.IPAddressChecker;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.example.zookeeperusersnodes.zookeeper.ZooKeeperInitializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.zookeeperusersnodes.constants.NodePaths.ELECTION_PATH;

@Service
public class ZooKeeperServiceImpl implements ZooKeeperService {
    private String currentZNode;

    @Autowired
    private ZooKeeperInitializer zooKeeperInitializer;
    private final ZooKeeper zooKeeper;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                List<String> children = zooKeeper.getChildren(NodePaths.ALL_NODES_PATH + newNodeName, true);

                if(children.isEmpty())
                    zooKeeper.delete(NodePaths.ALL_NODES_PATH + newNodeName, -1);
                else {
                    for (String child : children) {
                        String childPath = NodePaths.ALL_NODES_PATH + newNodeName + "/" + child;
                        zooKeeper.delete(childPath, -1);
                    }
                }
            }

            this.removeZNodeFromLiveNodes(username);
        }
        catch (KeeperException | InterruptedException e) {
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeUserZNode(String username) {
        String newNodeName = "/" + username;

        try {
            if(zooKeeper.exists(NodePaths.USERS_PATH + newNodeName, false) != null) {
                List<String> children = zooKeeper.getChildren(NodePaths.USERS_PATH + newNodeName, true);

                if(children.isEmpty())
                    zooKeeper.delete(NodePaths.USERS_PATH + newNodeName, -1);
                else {
                    for (String child : children) {
                        String childPath = NodePaths.USERS_PATH + newNodeName + "/" + child;
                        zooKeeper.delete(childPath, -1);
                    }
                }
            }
        }
        catch (KeeperException | InterruptedException e) {
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUserZNode(UserDTO userDTO, boolean userAdded) {
        String absoluteNodePath = NodePaths.USERS_PATH + "/" + userDTO.getUsername();
        Stat stat;

        try {
            if((stat = zooKeeper.exists(absoluteNodePath, false)) == null) {
                String jsonString = objectMapper.writeValueAsString(userDTO);

                byte[] userData = jsonString.getBytes();

                zooKeeper.create(absoluteNodePath, userData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            else {
                // reconnected
                userDTO.setOnline(true);

                byte[] updatedUserData = objectMapper.writeValueAsBytes(userDTO);

                zooKeeper.setData(absoluteNodePath, updatedUserData, stat.getVersion());
            }
        }
        catch (KeeperException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logoutUser(String username) {
        String absoluteNodePath = NodePaths.USERS_PATH + "/" + username;

        try {
            Stat stat = zooKeeper.exists(absoluteNodePath, false);

            byte[] userData = zooKeeper.getData(absoluteNodePath, false, stat);

            UserDTO userDTO = objectMapper.readValue(userData, UserDTO.class);

            userDTO.setOnline(false);

            byte[] updatedUserData = objectMapper.writeValueAsBytes(userDTO);

            zooKeeper.setData(absoluteNodePath, updatedUserData, stat.getVersion());
        }
        catch (KeeperException | InterruptedException | IOException e) {
//            throw new RuntimeException(e);
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

            // MESSAGE will have: { From: username, Text: text }
            // if "From" is null then it is a global message
            zooKeeper.create(NodePaths.ALL_NODES_PATH + newNodeName + "/message-", message.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        }
        catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void addMessageZNode(UserMessageDTO userMessageDTO) {
        // Scenario: Zika sends Pera message:
        // { From: "Zika", To: "Pera",  "De si pero" }
        // This needed to be created in both /users/pera and /users/zika

        String senderName = "/" + userMessageDTO.getFrom();
        String receiverName = "/" + userMessageDTO.getTo();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(userMessageDTO);
            byte[] messageData = jsonString.getBytes();

            // # If the user does not exist? This should be error, because logged user exist...
//            if(zooKeeper.exists(NodePaths.USERS_PATH + newNodeName, false) == null) {
//                zooKeeper.create(NodePaths.USERS_PATH + newNodeName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//            }

            // If user already sent some messages we create only its children with new messages
            // MESSAGE will have: { From: username, Text: text }
            // if "From" is null then it is a global message
            zooKeeper.create(NodePaths.USERS_PATH + senderName + "/message-", messageData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            zooKeeper.create(NodePaths.USERS_PATH + receiverName + "/message-", messageData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        }
        catch (KeeperException | InterruptedException | JsonProcessingException e) {
//            throw new RuntimeException(e);
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
