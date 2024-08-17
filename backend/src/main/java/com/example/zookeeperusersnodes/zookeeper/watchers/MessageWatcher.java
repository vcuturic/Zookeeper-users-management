package com.example.zookeeperusersnodes.zookeeper.watchers;
import com.example.zookeeperusersnodes.dto.UserDTO;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.services.interfaces.MessageService;
import com.example.zookeeperusersnodes.services.interfaces.WebSocketService;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import com.example.zookeeperusersnodes.utils.CommonUtils;
import com.example.zookeeperusersnodes.zookeeper.DataStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageWatcher implements Watcher {
    private final ZooKeeper zooKeeper;
    private final MessageService messageService;
    private final WebSocketService webSocketService;
    private final ZooKeeperService zooKeeperService;
    private CommonUtils commonUtils;
    ObjectMapper objectMapper = new ObjectMapper();

    public MessageWatcher(ZooKeeper zooKeeper, MessageService messageService, WebSocketService webSocketService, ZooKeeperService zooKeeperService, CommonUtils commonUtils) {
        this.zooKeeper = zooKeeper;
        this.messageService = messageService;
        this.webSocketService = webSocketService;
        this.zooKeeperService = zooKeeperService;
        this.commonUtils = commonUtils;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            try {
                // Scenario: When for ex. Pera, Mika, Zika sends a message
                String userPath = watchedEvent.getPath();
                // We get all children from the lucky gentleman
                List<String> children = zooKeeper.getChildren(userPath, false);
                // Sort them (it's a sequential node)
                children.sort(String::compareTo);
                // Read last message
                byte[] data = zooKeeper.getData(userPath + "/" + children.getLast(), false, null);

                String message = new String(data);

                UserMessageDTO userMessage = objectMapper.readValue(message, UserMessageDTO.class);

                List<String> expectServers = new ArrayList<>();
                List<String> sendToServers = new ArrayList<>();

                System.out.println("[debug]: MessageWatcher: DataStorage users: ");
                System.out.println(UserDTO.getUsernames(DataStorage.getUserList()));

                String currentWebSocketInstance = this.commonUtils.getCurrentWebSocketInstance();
                System.out.println("[debug]: MessageWatcher: currentWebSocketInstance: " + currentWebSocketInstance);

                String senderAddress = DataStorage.getUser(userMessage.getFrom()).getAddress();
                String receiverAddress = DataStorage.getUser(userMessage.getTo()).getAddress();

                System.out.println("[debug]: MessageWatcher: senderAddress: " + senderAddress);
                System.out.println("[debug]: MessageWatcher: receiverAddress: " + receiverAddress);

                String senderWebSocketInstance = commonUtils.getWebSocketInstanceByFrontendAddress(senderAddress);
                String receiverWebSocketInstance = commonUtils.getWebSocketInstanceByFrontendAddress(receiverAddress);

                System.out.println("[debug]: MessageWatcher: senderWebSocketInstance: " + senderWebSocketInstance);
                System.out.println("[debug]: MessageWatcher: receiverWebSocketInstance: " + receiverWebSocketInstance);

                // Scenario: Leader is 9091, Pera and Zika are on 9090 and 9092 respective --> broadcast with exception to 9091
                if(!senderWebSocketInstance.equals(currentWebSocketInstance) && !receiverWebSocketInstance.equals(currentWebSocketInstance)) {
                    expectServers.add(currentWebSocketInstance);
                    this.webSocketService.broadcastMessage(expectServers, userMessage);
                }
                else {
                    /* Scenario #2: Sender or receiver is connected to Leader node (for example Pera 9090 and Mika 9091(Leader)) --> broadcast except for 9091 and 9092 */
                    if(senderWebSocketInstance.equals(currentWebSocketInstance)) {
                        sendToServers.add(receiverWebSocketInstance);
                    }
                    if(receiverWebSocketInstance.equals(currentWebSocketInstance)) {
                        sendToServers.add(senderWebSocketInstance);
                    }

                    System.out.println(userPath + " New MESSAGE: " + userMessage.getText());

                    this.messageService.sendMessage(userMessage); // This sends message from current webSocket
                    this.webSocketService.broadcastMessageTo(sendToServers, userMessage);
                }
            }
            catch (KeeperException | InterruptedException | JsonProcessingException e) {
//                    throw new RuntimeException(e);
            }
        }
    }
}
