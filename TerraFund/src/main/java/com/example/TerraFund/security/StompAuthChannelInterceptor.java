package com.example.TerraFund.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * SECURITY: authenticates STOMP WebSocket connections with the JWT sent in the
 * "Authorization: Bearer <token>" header of the CONNECT frame. Previously /ws
 * was completely unauthenticated, which allowed anonymous clients to connect
 * and messages to be sent under any identity.
 *
 * The resulting Principal (the user's email) is used by ChatController to
 * derive the real sender identity and to route user-destination messages.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            try {
                if (!jwtService.validateToken(token)) {
                    throw new MessagingException("Invalid or expired token");
                }
            } catch (JwtException e) {
                throw new MessagingException("Invalid or expired token");
            }

            String email = jwtService.getEmailFromToken(token);
            accessor.setUser(new StompPrincipal(email));
        }
        return message;
    }

    record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
