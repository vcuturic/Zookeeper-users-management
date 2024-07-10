package com.example.zookeeperusersnodes.zookeeper.watchers;
import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.realtime.interfaces.MessageService;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public class MessageWatcher implements Watcher {
    private final ZooKeeper zooKeeper;
    private final MessageService messageService;

    public MessageWatcher(ZooKeeper zooKeeper, MessageService messageService) {
        this.zooKeeper = zooKeeper;
        this.messageService = messageService;
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

                System.out.println(userPath + " New MESSAGE: " + message);
                // TODO Send message to message window. (websocket)
                String[] parts = userPath.split("/");
                String username = parts[parts.length - 1];
                this.messageService.sendMessage(new UserMessageDTO(username, message));
            }
            catch (KeeperException | InterruptedException e) {
//                    throw new RuntimeException(e);
            }
        }
    }
}
