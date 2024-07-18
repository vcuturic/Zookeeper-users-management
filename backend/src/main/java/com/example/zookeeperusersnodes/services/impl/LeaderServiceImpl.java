package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.zookeeper.ClusterInfo;
import com.example.zookeeperusersnodes.services.interfaces.LeaderService;
import org.springframework.stereotype.Service;

@Service
public class LeaderServiceImpl implements LeaderService {
    @Override
    public boolean isThisNodeLeader(String nodeName) {
        return ClusterInfo.getClusterInfo().getLeaderNode().equals(nodeName);
    }

    @Override
    public String getLeaderAddress() {
        return ClusterInfo.getClusterInfo().getLeaderAddress();
    }
}
