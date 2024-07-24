package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.services.interfaces.UserService;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {
    private final Map<String, Long> userActivity = new ConcurrentHashMap<>();

    @Override
    public void userLeft(String username) {
        this.userActivity.remove(username);
    }

    public Map<String, Long> getUserActivity() {
        return userActivity;
    }
}
