package com.example.wsgateway.auth;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Runs before the WebSocket handshake is completed.
 * Validates the token from the query string and stores
 * the username in session attributes for later use.
 *
 * Connect URL example:
 *   ws://localhost:8080/ws?token=abc123
 *
 * Replace validateToken() with your real JWT/auth logic.
 */
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        String query = request.getURI().getQuery(); // e.g. "token=abc123"

        if (query == null || !query.startsWith("token=")) {
            System.out.println("Rejected WS connection — no token provided");
            return false; // Reject the handshake
        }

        String token = query.substring("token=".length());
        String username = validateToken(token);

        if (username == null) {
            System.out.println("Rejected WS connection — invalid token: " + token);
            return false; // Reject the handshake
        }

        // Store identity so the handler can access it via session.getAttributes()
        attributes.put("username", username);
        System.out.println("WS handshake approved for user=" + username);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // Nothing needed here
    }

    /**
     * TODO: Replace this stub with real JWT validation.
     *
     * Example with jjwt library:
     *   Claims claims = Jwts.parserBuilder()
     *       .setSigningKey(secretKey)
     *       .build()
     *       .parseClaimsJws(token)
     *       .getBody();
     *   return claims.getSubject();
     *
     * Returns the username/subject on success, null on failure.
     */
    private String validateToken(String token) {
        // --- STUB: accept any non-empty token for now ---
        if (token == null || token.isBlank()) return null;

        // Fake user lookup — replace with actual JWT decode
        if (token.equals("admin-token"))  return "admin";
        if (token.equals("trader-token")) return "trader1";

        // In production: decode JWT and return claims.getSubject()
        return null;
    }
}