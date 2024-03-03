package com.example.zookeeperusersnodes.zookeeper;

import java.util.ArrayList;
import java.util.List;

public class ClusterInfo {
    private List<String> allNodes = new ArrayList<>();
    private List<String> liveNodes = new ArrayList<>();
    private String leaderNode;
    private static ClusterInfo clusterInfo = new ClusterInfo();

    public static ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public List<String> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(List<String> allNodes) {
        this.allNodes = allNodes;
    }

    public List<String> getLiveNodes() {
        return liveNodes;
    }

    public void setLiveNodes(List<String> liveNodes) {
        this.liveNodes = liveNodes;
    }

    public String getLeaderNode() {
        return leaderNode;
    }

    public void setLeaderNode(String leaderNode) {
        this.leaderNode = leaderNode;
    }
}
