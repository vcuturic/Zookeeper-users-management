package com.example.zookeeperusersnodes.zookeeper.impl;
import com.example.zookeeperusersnodes.zookeeper.interfaces.WatchersManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Component
public class WatchersManagerImpl implements WatchersManager {
    private final Set<String> watchedNodes;

    public WatchersManagerImpl(Set<String> watchedNodes) {
        this.watchedNodes = watchedNodes;
    }

    @Override
    public void addWatcher(String path) {
        watchedNodes.add(path);
    }

    @Override
    public void removeWatcher(String path) {
        watchedNodes.remove(path);
    }

    @Override
    public boolean hasWatcher(String path) {
        return watchedNodes.contains(path);
    }
}
