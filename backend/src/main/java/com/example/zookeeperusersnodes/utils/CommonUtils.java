package com.example.zookeeperusersnodes.utils;

import com.example.zookeeperusersnodes.constants.NodePaths;
import com.example.zookeeperusersnodes.constants.NodeTypes;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {
    public static List<String> getServerNodes(ZooKeeper zooKeeper) {
        try {
            List<String> serverNodes = zooKeeper.getChildren(NodePaths.ELECTION_PATH, false);
            List<String> serverNodesAddresses = new ArrayList<>();

            for (String serverNode : serverNodes) {
                String serverNodePath = NodePaths.ELECTION_PATH + "/" + serverNode;
                byte[] data = zooKeeper.getData(serverNodePath, false, null);

                if (data != null && data.length > 0) {
                    String serverNodeAddress = new String(data);

                    serverNodesAddresses.add(serverNodeAddress);
                }
            }

            return serverNodesAddresses;
        }
        catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
