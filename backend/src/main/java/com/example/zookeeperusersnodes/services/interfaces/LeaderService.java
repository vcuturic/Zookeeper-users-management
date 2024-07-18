package com.example.zookeeperusersnodes.services.interfaces;

public interface LeaderService {
    boolean isThisNodeLeader(String nodeName);
    String getLeaderAddress();
}
