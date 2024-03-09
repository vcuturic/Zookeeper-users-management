package com.example.zookeeperusersnodes.bl;
import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.example.zookeeperusersnodes.zookeeper.ZooKeeperInitializer;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
