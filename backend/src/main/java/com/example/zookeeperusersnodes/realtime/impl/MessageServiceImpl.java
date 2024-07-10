package com.example.zookeeperusersnodes.realtime.impl;

import com.example.zookeeperusersnodes.dto.UserMessageDTO;
import com.example.zookeeperusersnodes.realtime.interfaces.MessageService;
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
