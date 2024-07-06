package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.zookeeper.DataStorage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public void Login() {
        // TODO when user logs in just add it to DataStorage for now
//        DataStorage.addPersonToList(person);
    }
}
