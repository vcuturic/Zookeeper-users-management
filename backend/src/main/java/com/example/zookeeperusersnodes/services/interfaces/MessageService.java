package com.example.zookeeperusersnodes.services.interfaces;

import com.example.zookeeperusersnodes.dto.UserMessageDTO;

public interface MessageService {
    String DESTINATION_ROUTE = "/topic/messages";

    void sendMessage(UserMessageDTO userMessage);
}
