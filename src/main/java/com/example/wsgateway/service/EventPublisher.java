package com.example.wsgateway.service;

import com.example.wsgateway.handler.GatewayWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    // simulate ORDER service
    @Scheduled(fixedRate = 8000)
    public void publishOrderEvent() throws Exception {

        GatewayWebSocketHandler.broadcast("orders",
                "Order Executed: IBM 100 shares");
    }

    // simulate TRADE service
    @Scheduled(fixedRate = 10000)
    public void publishTradeEvent() throws Exception {

        GatewayWebSocketHandler.broadcast("trades",
                "Trade Completed: AAPL @ 185");
    }

    // simulate MARKET DATA service
    @Scheduled(fixedRate = 5000)
    public void publishMarketData() throws Exception {

        GatewayWebSocketHandler.broadcast("symbol",
                "IBM price updated to 121.50");
    }
}