package com.example.zookeeperusersnodes.zookeeper;
import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.constants.NodeTypes;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.services.interfaces.MessageService;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.services.interfaces.WebSocketService;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import com.example.zookeeperusersnodes.utils.CommonUtils;
import com.example.zookeeperusersnodes.zookeeper.interfaces.WatchersManager;
import com.example.zookeeperusersnodes.zookeeper.watchers.UsersWatcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.example.zookeeperusersnodes.utils.CommonUtils.findDifferentElement;

@Component
public class LeaderElection implements Watcher {

    @Value("${server.port}")
    private int serverPort;
    private static final String ELECTION_PATH = NodePaths.ELECTION_PATH;
    private static final String ALL_NODES_PATH = NodePaths.ALL_NODES_PATH;
    private static final String LIVE_NODES_PATH = NodePaths.LIVE_NODES_PATH;
    private String currentZNode;
    private final ZooKeeper zooKeeper;
    private final NotificationService notificationService;
    @Autowired
    private ZooKeeperService zooKeeperService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private CommonUtils commonUtils;
    ObjectMapper objectMapper = new ObjectMapper();

    private final WatchersManager watcherManager;
    private final MessageService messageService;

    public LeaderElection(ZooKeeperInitializer zooKeeperInitializer, NotificationService notificationService, WatchersManager watcherManager, MessageService messageService) {
        this.zooKeeper = zooKeeperInitializer.getZooKeeperInstance();
        this.notificationService = notificationService;
        this.watcherManager = watcherManager;
        this.messageService = messageService;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getPath().equals(ELECTION_PATH)) {
            if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
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
                        String newChild, deletedChild;

                        if(children.size() > ClusterInfo.getClusterInfo().getAllNodes().size()) {
                            // Addition occurred
                            newChild = findDifferentElement(children, ClusterInfo.getClusterInfo().getAllNodes());
                            int nodeType = getNodeType(newChild);

                            ClusterInfo.getClusterInfo().getAllNodes().add(newChild);

                            this.notificationService.nodeConnectedOfflineNotification(newChild, nodeType);
                            this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), new NodeDTO(newChild, nodeType, NodeOperations.OPERATION_CONNECT_OFFLINE));
                        }
                        else {
                            // Deletion occurred
                            deletedChild = findDifferentElement(ClusterInfo.getClusterInfo().getAllNodes(), children);

                            ClusterInfo.getClusterInfo().getAllNodes().remove(deletedChild);

                            this.notificationService.nodeDeletedNotification(deletedChild);
                            this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), new NodeDTO(deletedChild, NodeOperations.OPERATION_DELETE));
                        }
                    }
                    catch (KeeperException | InterruptedException e) {
//                        throw new RuntimeException(e);
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
    public int getNodeType(String nodeName) {
        try {
            List<String> serverNodes = zooKeeper.getChildren(NodePaths.ELECTION_PATH, false);

            for (String serverNode : serverNodes) {
                String serverNodePath = ELECTION_PATH + "/" + serverNode;
                byte[] data = zooKeeper.getData(serverNodePath, false, null);

                if (data != null && data.length > 0) {
                    String serverNodeAddress = new String(data);

                    if(nodeName.equals(serverNodeAddress))
                        return NodeTypes.ZNODE_TYPE_SERVER;
                }
            }

            return NodeTypes.ZNODE_TYPE_USER;
        }
        catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
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

    public void getLatestUserInfo() {
        try {
            // UPDATE all nodes nad live nodes.
            List<String> userNodes = zooKeeper.getChildren(NodePaths.USERS_PATH, false);
            DataStorage.getUserList().clear();

            for (String userName : userNodes) {
                String childPath = NodePaths.USERS_PATH + "/" + userName;

                byte[] data = zooKeeper.getData(childPath, false, null);

                String userString = new String(data);

                UserDTO userDTO = objectMapper.readValue(userString, UserDTO.class);

                DataStorage.getUserList().add(userDTO);
            }
        }
        catch (KeeperException | InterruptedException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void reevaluateLeadership() throws InterruptedException, KeeperException {
        // TODO reelection to be only called when an leader disconnects
        List<String> children = zooKeeper.getChildren(ELECTION_PATH, false);

        if(children.isEmpty()) {
            // No more leaders, server shutting down
            this.onServerDisconnect();
        }
        else {
            children.sort(String::compareTo);

            if (currentZNode.equals(ELECTION_PATH + "/" + children.getFirst())) {
                System.out.println("I am the leader! " + currentZNode);

                this.getLatestClusterInfo();
                this.getLatestUserInfo();
                // CurrentZNode is the leader, add its Watcher() to /all_nodes and /live_nodes
                zooKeeper.addWatch(ALL_NODES_PATH, this, AddWatchMode.PERSISTENT);
                zooKeeper.addWatch(LIVE_NODES_PATH, this, AddWatchMode.PERSISTENT);
                ClusterInfo.getClusterInfo().setLeaderNode(currentZNode);
                ClusterInfo.getClusterInfo().setLeaderAddress(getServerInfo());

                // Problems: whenever a new instance connects Messenger and UsersWatcher got a new instance
                Messenger messenger = new Messenger(zooKeeper, watcherManager, messageService, zooKeeperService, webSocketService, commonUtils);
                messenger.init();

                if(!watcherManager.hasUsersWatcher()) {
                    watcherManager.addUsersWatcher();
                    UsersWatcher usersWatcher = new UsersWatcher(zooKeeperService, zooKeeper, notificationService, webSocketService, watcherManager);
                    usersWatcher.init();
                }

            }
            else {
                System.out.println("I am not the leader." + currentZNode);
                ClusterInfo.getClusterInfo().setLeaderNode(ELECTION_PATH + "/" + children.getFirst());
                // TODO We need to get serverInfo from the leader node and set it here, that's why we are
                // getting null
                String leaderPath = ELECTION_PATH + "/" + children.getFirst();
                Stat stat = zooKeeper.exists(leaderPath, true);
                byte[] data = zooKeeper.getData(leaderPath, false, stat);
                String leaderAddress = new String(data);
//                System.out.println("New Leader:  " + leaderPath + leaderAddress);
                ClusterInfo.getClusterInfo().setLeaderAddress(leaderAddress);
                // TODO Communication with leader node (one route should be this zNode asking for update from leader)
                // for ex. This zNode connected later, and don't have the relevant data
                String clusterInfoUrl = "http://" + leaderAddress + "/comm/clusterInfo"; // URL of ServiceA
                ClusterInfo.updateClusterInfo(Objects.requireNonNull(restTemplate.getForObject(clusterInfoUrl, ClusterInfo.class)));

                // Get Newest personInfo
                String userInfoUrl = "http://" + leaderAddress + "/comm/userInfo"; // URL of ServiceA
                ResponseEntity<List<UserDTO>> responseEntity = restTemplate.exchange(
                        userInfoUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<UserDTO>>() {}
                );

                List<UserDTO> userList = responseEntity.getBody();

                if (userList != null) {
                    DataStorage.updateUserList(userList);
                }
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

        this.zooKeeperService.setCurrentZNode(currentZNode);
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
                this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), new NodeDTO(addedNode, getNodeType(addedNode), NodeOperations.OPERATION_RECONNECT));
            }
            else {
                this.notificationService.nodeConnectedOnlineNotification(addedNode);
                this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), new NodeDTO(addedNode, getNodeType(addedNode), NodeOperations.OPERATION_CONNECT_ONLINE));
            }

            ClusterInfo.getClusterInfo().getLiveNodes().clear();
            ClusterInfo.getClusterInfo().getLiveNodes().addAll(children);
        }
        else {
            // DISCONNECT occurred
            previousChildren.removeAll(currentChildren);

            Iterator<String> iterator = previousChildren.iterator();
            String disconnectedNode = iterator.hasNext() ? iterator.next() : null;

            this.notificationService.nodeDisconnectedNotification(disconnectedNode);
            this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), new NodeDTO(disconnectedNode, getNodeType(disconnectedNode), NodeOperations.OPERATION_DISCONNECT));

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
                System.out.println(" ");
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
