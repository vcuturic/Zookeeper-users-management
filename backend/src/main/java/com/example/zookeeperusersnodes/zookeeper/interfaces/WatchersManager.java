package com.example.zookeeperusersnodes.zookeeper.interfaces;

import java.util.HashSet;
import java.util.Set;

public interface WatchersManager {
    void addMessageWatcher(String path);
    void removeMessageWatcher(String path);
    boolean hasMessageWatcher(String path);
    void addUserWatcher(String path);
    void removeUserWatcher(String path);
    boolean hasUserWatcher(String path);
    void addUsersWatcher();
    void removeUsersWatcher();
    boolean hasUsersWatcher();

    void addMessagesWatcher();
    void removeMessagesWatcher();
    boolean hasMessagesWatcher();
}
