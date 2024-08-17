package com.example.zookeeperusersnodes.zookeeper.watchers;

import com.example.zookeeperusersnodes.constants.NodeOperations;
import com.example.zookeeperusersnodes.constants.NodeTypes;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.services.interfaces.WebSocketService;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.example.zookeeperusersnodes.zookeeper.DataStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Optional;

/*
Used to watch single user, watches .data change in zNode to regulate online/offline status
*/
public class UserWatcher implements Watcher {

    private final ZooKeeper zooKeeper;
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserWatcher(ZooKeeper zooKeeper, NotificationService notificationService, WebSocketService webSocketService) {
        this.zooKeeper = zooKeeper;
        this.notificationService = notificationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
            String userPath = watchedEvent.getPath();

            try {
                Stat stat = zooKeeper.exists(userPath, false);

                byte[] userData = zooKeeper.getData(userPath, false, stat);

                UserDTO userDTO = objectMapper.readValue(userData, UserDTO.class);

                String[] parts = userPath.split("/");
                String updatedNode = parts[parts.length - 1];

                // Connected, Deleted are regulated in ZooKeeperService
                if(userDTO.isOnline()) {
                    // User reconnected
//                    System.out.println("UserWatcher triggered RECONNECT on: " + userPath);

                    Optional<UserDTO> userOpt = DataStorage.getUserList().stream()
                            .filter(prs -> prs.getUsername().equals(userDTO.getUsername()))
                            .findFirst();

                    userOpt.ifPresent(prs -> {
                        prs.setOnline(true);
                    });

//                    System.out.println("UserWatcher personList: ");
//                    System.out.println(UserDTO.getUsernames(DataStorage.getPersonList()));

                    this.notificationService.nodeReconnectedNotification(updatedNode);
                    this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), new NodeDTO(updatedNode, NodeTypes.ZNODE_TYPE_USER, NodeOperations.OPERATION_RECONNECT));
                }
                else {
                    // User disconnected
//                    System.out.println("UserWatcher triggered DISCONNECT on: " + userPath);

                    Optional<UserDTO> userOpt = DataStorage.getUserList().stream()
                            .filter(prs -> prs.getUsername().equals(userDTO.getUsername()))
                            .findFirst();

                    userOpt.ifPresent(prs -> {
                        prs.setOnline(false);
                    });

//                    System.out.println("UserWatcher personList: ");
//                    System.out.println(UserDTO.getUsernames(DataStorage.getPersonList()));

                    this.notificationService.nodeDisconnectedNotification(updatedNode);
                    this.webSocketService.broadcast(ClusterInfo.getClusterInfo().getLeaderAddress(), new NodeDTO(updatedNode, NodeTypes.ZNODE_TYPE_USER, NodeOperations.OPERATION_DISCONNECT));
                }
            }
            catch (KeeperException | InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
