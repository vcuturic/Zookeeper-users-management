package com.example.zookeeperusersnodes.services.interfaces;

import com.example.zookeeperusersnodes.dto.NodeDTO;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public interface ZooKeeperService {
    String getCurrentZNode();
    void setCurrentZNode(String currentZNode);
    List<NodeDTO> getZookeeperServerNodes();
    List<NodeDTO> getAllNodesChildren();
    List<String> getLiveNodesChildren();
    String getLeaderInfo();
    boolean isThisNodeLeader(String nodeName);
    String getLeaderAddress();
    void addZNode(String username);
    void addZNode(String username, boolean userAdded);
    void removeZNodeFromLiveNodes(String username);
    void removeZNode(String username);
    void addMessageZNode(String username, String message);
    List<String> getServerNodesFromAllNodes(List<String> allNodes);
}
