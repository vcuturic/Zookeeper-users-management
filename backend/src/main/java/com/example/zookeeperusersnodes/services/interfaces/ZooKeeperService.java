package com.example.zookeeperusersnodes.services.interfaces;

public interface ZooKeeperService {
    String getCurrentZNode();
    void setCurrentZNode(String currentZNode);
    String getZNodeAddress(String zNode);
}
