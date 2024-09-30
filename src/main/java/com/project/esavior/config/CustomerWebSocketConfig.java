package com.project.esavior.config;

import com.project.esavior.websocket.websocket.CustomerWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class CustomerWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Đăng ký WebSocket endpoint cho khách hàng
        registry.addHandler(new CustomerWebSocketHandler(), "/ws/customer")
                .setAllowedOriginPatterns("*");
    }
}
