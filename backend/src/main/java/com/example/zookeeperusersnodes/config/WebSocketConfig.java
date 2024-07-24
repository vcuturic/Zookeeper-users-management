package com.example.zookeeperusersnodes.config;

import com.example.zookeeperusersnodes.services.impl.WebSocketHandler;
import com.example.zookeeperusersnodes.services.interfaces.MessageService;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationService notificationService;
    private final MessageService messageService;

    public WebSocketConfig(NotificationService notificationService, MessageService messageService) {
        this.notificationService = notificationService;
        this.messageService = messageService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(this.notificationService, this.messageService), "/ws-endpoint2")
                .setAllowedOrigins("*");
    }
}
