package com.example.zookeeperusersnodes.zookeeper.interfaces;

import java.util.HashSet;
import java.util.Set;

public interface WatchersManager {
    void addWatcher(String path);
    void removeWatcher(String path);
    boolean hasWatcher(String path);
}
