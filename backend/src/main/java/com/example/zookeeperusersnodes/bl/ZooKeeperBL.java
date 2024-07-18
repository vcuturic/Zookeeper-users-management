package com.example.zookeeperusersnodes.bl;

import com.example.zookeeperusersnodes.dto.NodeDTO;

import java.util.List;

public interface ZooKeeperBL {
    List<NodeDTO> getZookeeperServerNodes();
    List<String> getAllNodesChildren();
    List<NodeDTO> getAllNodesChildrenInfo();
    List<String> getLiveNodesChildren();
    String getLeaderInfo();
    String getLeaderAddress();
    void addZNode(String username);
    void addZNode(String username, boolean userAdded);
    void removeZNodeFromLiveNodes(String username);
    void removeZNode(String username);
    void addMessageZNode(String username, String message);
}
