package com.example.wsgateway.config;

import com.example.wsgateway.auth.AuthHandshakeInterceptor;
import com.example.wsgateway.handler.GatewayWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public GatewayWebSocketHandler gatewayWebSocketHandler() {
        return new GatewayWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gatewayWebSocketHandler(), "/ws")
                .addInterceptors(new AuthHandshakeInterceptor()) // Auth runs first
                .setAllowedOrigins("*");
    }
}