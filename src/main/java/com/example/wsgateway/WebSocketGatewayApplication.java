package com.example.wsgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebSocketGatewayApplication {

    public static void main(String[] args) {

        SpringApplication.run(WebSocketGatewayApplication.class, args);

        System.out.println("WebSocket Gateway started successfully...");
    }
}
