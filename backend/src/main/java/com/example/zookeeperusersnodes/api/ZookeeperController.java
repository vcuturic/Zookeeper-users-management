package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.config.ZooKeeperInitializer;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/zookeeper")
public class ZookeeperController {

    @Autowired
    private ZooKeeperInitializer zookeeperInitializer;

    @GetMapping("/allnodes")
    public List<NodeDTO> getAllZookeeperNodes() {
        List<NodeDTO> znodes = new ArrayList<>();

        this.populateZookeeperNodes("/", znodes);

        return znodes;
    }

    public void populateZookeeperNodes(String path, List<NodeDTO> parentsChildren) {
        ZooKeeper zooKeeper = zookeeperInitializer.getZooKeeperInstance();

        List<String> children = null;

        try {
            children = zooKeeper.getChildren(path, false);
        } catch (KeeperException e) {
            // SESSION EXCEPTION, NO NODE EXCEPTION
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
