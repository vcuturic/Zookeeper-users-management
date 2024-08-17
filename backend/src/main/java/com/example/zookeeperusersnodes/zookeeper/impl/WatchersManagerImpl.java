package com.example.zookeeperusersnodes.zookeeper.impl;
import com.example.zookeeperusersnodes.zookeeper.interfaces.WatchersManager;
import org.springframework.stereotype.Component;

import java.util.Set;

/*

*/
@Component
public class WatchersManagerImpl implements WatchersManager {
    private final Set<String> watchedUserMessagesNodes;
    private final Set<String> watchedUserNodes;
    private boolean usersNodeWatched;
    private boolean usersMessengerSet;

    public WatchersManagerImpl(Set<String> watchedUserMessagesNodes, Set<String> watchedUserNodes) {
        this.watchedUserMessagesNodes = watchedUserMessagesNodes;
        this.watchedUserNodes = watchedUserNodes; 
        this.usersNodeWatched = false;
    }

    @Override
    public void addMessageWatcher(String path) {
        watchedUserMessagesNodes.add(path);
    }

    @Override
    public void removeMessageWatcher(String path) {
        watchedUserMessagesNodes.remove(path);
    }

    @Override
    public boolean hasMessageWatcher(String path) {
        return watchedUserMessagesNodes.contains(path);
    }

    @Override
    public void addUserWatcher(String path) {
        watchedUserNodes.add(path);
    }

    @Override
    public void removeUserWatcher(String path) {
        watchedUserNodes.remove(path);
    }

    @Override
    public boolean hasUserWatcher(String path) {
        return watchedUserNodes.contains(path);
    }

    @Override
    public void addUsersWatcher() {
        usersNodeWatched = true;
    }

    @Override
    public void removeUsersWatcher() {
        usersNodeWatched = false;
    }

    @Override
    public boolean hasUsersWatcher() {
        return usersNodeWatched;
    }

    @Override
    public void addMessagesWatcher() {
        usersMessengerSet = true;
    }

    @Override
    public void removeMessagesWatcher() {
        usersMessengerSet = false;
    }

    @Override
    public boolean hasMessagesWatcher() {
        return usersMessengerSet;
    }
}
