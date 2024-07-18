package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import org.springframework.stereotype.Service;

@Service
public class ZooKeeperServiceImpl implements ZooKeeperService {
    private String currentZNode;

    @Override
    public String getCurrentZNode() {
        return this.currentZNode;
    }

    @Override
    public void setCurrentZNode(String currentZNode) {
        this.currentZNode = currentZNode;
    }

    @Override
    public String getZNodeAddress(String zNode) {
        return null;
    }
}
