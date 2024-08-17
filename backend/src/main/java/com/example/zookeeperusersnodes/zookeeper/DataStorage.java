package com.example.zookeeperusersnodes.zookeeper;

import com.example.zookeeperusersnodes.dto.UserDTO;
import org.apache.catalina.User;

import java.util.ArrayList;
import java.util.List;

public final class DataStorage {
    private static List<UserDTO> userList = new ArrayList<>();

    private DataStorage() {}

    public static List<UserDTO> getUserList() {
        return userList;
    }

    public static void addUserToList(UserDTO user) {
        userList.add(user);
    }

    public static UserDTO getUser(String username) {
        for (UserDTO user : userList) {
            if(user.getUsername().equals(username))
                return user;
        }

        return null;
    }

    public static void updateUserList(List<UserDTO> updatedUserList) {
        userList.clear();
        userList.addAll(updatedUserList);
    }


}
