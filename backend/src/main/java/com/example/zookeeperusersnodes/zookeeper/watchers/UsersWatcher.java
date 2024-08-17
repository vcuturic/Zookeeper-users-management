package com.example.zookeeperusersnodes.zookeeper.watchers;

import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.constants.NodeTypes;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.services.interfaces.WebSocketService;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.example.zookeeperusersnodes.zookeeper.DataStorage;
import com.example.zookeeperusersnodes.zookeeper.interfaces.WatchersManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.zookeeper.*;
import java.util.List;

import static com.example.zookeeperusersnodes.utils.CommonUtils.findDifferentElement;

/*
Used to watch /users path, watches for NodeChildrenChanged event, and regulates, user Addition and Deletion
*/
public class UsersWatcher implements Watcher {

    private final ZooKeeperService zooKeeperService;
    private final ZooKeeper zooKeeper;

    private final NotificationService notificationService;
    private final WebSocketService webSocketService;
    private final WatchersManager watcherManager;
    ObjectMapper objectMapper = new ObjectMapper();

    public UsersWatcher(ZooKeeperService zooKeeperService, ZooKeeper zooKeeper, NotificationService notificationService, WebSocketService webSocketService, WatchersManager watcherManager) {
        this.zooKeeperService = zooKeeperService;
        this.zooKeeper = zooKeeper;
        this.notificationService = notificationService;
        this.webSocketService = webSocketService;
        this.watcherManager = watcherManager;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            try {
                List<String> children = zooKeeper.getChildren(NodePaths.USERS_PATH, null);
                String newChild, deletedChild;

                List<String> userNames = UserDTO.getUsernames(DataStorage.getUserList());
                System.out.println("---------USERS WATCHER: ---- ");
                System.out.println("Size from zookeeper: " + children.size());
                System.out.println("Size from DataStorage: " + DataStorage.getUserList().size());
                System.out.println("-----------------------------");

                if(children.size() > DataStorage.getUserList().size()) {
                    // Addition occurred
                    newChild = findDifferentElement(children, userNames);

                    // Get data from user
                    byte[] data = zooKeeper.getData(NodePaths.USERS_PATH + "/" + newChild, false, null);
                    String userString = new String(data);

                    UserDTO user = objectMapper.readValue(userString, UserDTO.class);

                    DataStorage.addUserToList(user);
                    System.out.println("User List: " + UserDTO.getUsernames(DataStorage.getUserList()));

                    this.addWatchersOnNodeChildren(NodePaths.USERS_PATH);

                    this.notificationService.nodeConnectedOfflineNotification(newChild, NodeTypes.ZNODE_TYPE_USER);
                    NodeDTO newNode = new NodeDTO(newChild, NodeTypes.ZNODE_TYPE_USER, user.isOnline() ? NodeOperations.OPERATION_CONNECT_ONLINE : NodeOperations.OPERATION_CONNECT_OFFLINE);
                    this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), newNode);
                }
                else if(children.size() < DataStorage.getUserList().size()){
                    // Deletion occurred
                    deletedChild = findDifferentElement(userNames, children);

                    DataStorage.getUserList().removeIf(user -> user.getUsername().equals(deletedChild));
                    System.out.println("(dlc)User List: " + UserDTO.getUsernames(DataStorage.getUserList()));

                    this.notificationService.nodeDeletedNotification(deletedChild);
                    this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), new NodeDTO(deletedChild, NodeOperations.OPERATION_DELETE));
                }

                // Not sure why this Watcher needs to reassign itself every time its triggered, AddWatchMode.PERSISTENT should manage continuity
                zooKeeper.addWatch(NodePaths.USERS_PATH, this, AddWatchMode.PERSISTENT);
            }
            catch (KeeperException | InterruptedException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addWatchersOnNodeChildren(String nodePath) throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren(nodePath, this);
        String childPath;

        // Here the children are usernames (Pera, Mika, Zika) - We need watchers on them
        for (String child : children) {
            childPath = nodePath + "/" + child;

            if(!watcherManager.hasUserWatcher(childPath)) {
                watcherManager.addUserWatcher(childPath);
                zooKeeper.addWatch(childPath, new UserWatcher(this.zooKeeper, this.notificationService, this.webSocketService), AddWatchMode.PERSISTENT);
                System.out.println("Created user watcher on: " + childPath);
            }
        }
    }

    public void init() throws InterruptedException, KeeperException {
        if (zooKeeperService.isThisNodeLeader(this.zooKeeperService.getCurrentZNode())) {
            System.out.println("Users Watcher initialized");

            zooKeeper.addWatch(NodePaths.USERS_PATH, this, AddWatchMode.PERSISTENT);

            addWatchersOnNodeChildren(NodePaths.USERS_PATH);
        }
    }
}
