package com.example.wsgateway.handler;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class GatewayWebSocketHandler extends TextWebSocketHandler {

    // Thread-safe set of all connected clients
    private static final Set<WebSocketSession> clients =
            new CopyOnWriteArraySet<>();

    // Thread-safe map: topic -> set of subscribed sessions
    private static final Map<String, Set<WebSocketSession>> topicSubscribers =
            new ConcurrentHashMap<>();

    // ---------------------------------------------------------------
    // Connection lifecycle
    // ---------------------------------------------------------------

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // At this point the HandshakeInterceptor has already validated the
        // token and stored the username in session attributes.
        String user = getUser(session);
        clients.add(session);
        System.out.println("Client connected: " + session.getId() + " (user=" + user + ")");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        clients.remove(session);
        // Remove this session from every topic it was subscribed to
        topicSubscribers.values().forEach(set -> set.remove(session));
        System.out.println("Client disconnected: " + session.getId()
                + " reason=" + status.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("Transport error for " + session.getId() + ": " + exception.getMessage());
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException ignored) {}
    }

    // ---------------------------------------------------------------
    // Message handling
    // ---------------------------------------------------------------

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();
        String user    = getUser(session);
        System.out.println("[" + user + "] Received: " + payload);

        if (payload.startsWith("subscribe:")) {
            handleSubscribe(session, payload);

        } else if (payload.startsWith("publish:")) {
            handlePublish(session, payload);

        } else {
            // Echo back unknown messages
            sendSafe(session, "Echo: " + payload);
        }
    }

    // subscribe:<topic>
    private void handleSubscribe(WebSocketSession session, String payload) throws Exception {
        // payload = "subscribe:orders"
        String[] parts = payload.split(":", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            sendSafe(session, "ERROR: subscribe requires a topic name");
            return;
        }
        String topic = parts[1].trim();

        topicSubscribers
                .computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>())
                .add(session);

        sendSafe(session, "Subscribed to " + topic);
        System.out.println(session.getId() + " subscribed to topic=" + topic);
    }

    // publish:<topic>:<data>
    // Using split(":", 3) so data can safely contain colons (e.g. timestamps)
    private void handlePublish(WebSocketSession session, String payload) {
        String[] parts = payload.split(":", 3);
        if (parts.length < 3) {
            sendSafe(session, "ERROR: publish format is publish:<topic>:<data>");
            return;
        }
        String topic = parts[1].trim();
        String data  = parts[2];

        broadcastToTopic(topic, data);
    }

    // ---------------------------------------------------------------
    // Broadcasting
    // ---------------------------------------------------------------

    /**
     * Called by EventPublisher (or any service) to push a message to
     * all sessions subscribed to a topic.
     */
    public static void broadcast(String topic, String message) {
        Set<WebSocketSession> subscribers = topicSubscribers.get(topic);
        if (subscribers == null || subscribers.isEmpty()) return;

        String formatted = "[" + topic + "] " + message;
        for (WebSocketSession s : subscribers) {
            sendSafe(s, formatted);
        }
    }

    /**
     * Instance-level broadcast used when a client publishes via WebSocket.
     */
    private void broadcastToTopic(String topic, String message) {
        broadcast(topic, message);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Send a message without throwing — logs errors instead so one bad
     * session doesn't crash the broadcast loop.
     */
    private static void sendSafe(WebSocketSession session, String text) {
        if (session == null || !session.isOpen()) return;
        try {
            // WebSocketSession.sendMessage is NOT thread-safe; synchronize per session
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            System.err.println("Failed to send to " + session.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Retrieve the authenticated username stored by the HandshakeInterceptor.
     */
    private String getUser(WebSocketSession session) {
        Object user = session.getAttributes().get("username");
        return user != null ? user.toString() : "anonymous";
    }
}