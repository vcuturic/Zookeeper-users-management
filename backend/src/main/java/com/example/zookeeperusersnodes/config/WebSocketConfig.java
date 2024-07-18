package com.example.zookeeperusersnodes.config;

import com.example.zookeeperusersnodes.services.impl.WebSocketHandler;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private NotificationService notificationService;

    public WebSocketConfig(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(this.notificationService), "/ws-endpoint2")
                .setAllowedOrigins("*");
    }
}
