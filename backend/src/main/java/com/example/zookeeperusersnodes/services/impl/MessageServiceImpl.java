package com.example.zookeeperusersnodes.services.impl;

import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.services.interfaces.MessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public MessageServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void sendMessage(UserMessageDTO userMessage) {
        simpMessagingTemplate.convertAndSend(DESTINATION_ROUTE, userMessage);
    }
}
