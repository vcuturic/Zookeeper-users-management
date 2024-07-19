package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.services.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {
    private Map<String, Long> userActivity = new ConcurrentHashMap<>();

    @Override
    public void userLeft(String username) {
        this.userActivity.remove(username);
    }

    public Map<String, Long> getUserActivity() {
        return userActivity;
    }
}
