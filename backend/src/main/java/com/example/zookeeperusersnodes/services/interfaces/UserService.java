package com.example.zookeeperusersnodes.services.interfaces;

import java.util.Map;

public interface UserService {
    void userLeft(String username);
    Map<String, Long> getUserActivity();
}
