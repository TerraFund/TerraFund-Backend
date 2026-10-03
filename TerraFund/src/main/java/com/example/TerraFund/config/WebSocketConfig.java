package com.example.TerraFund.config;

import com.example.TerraFund.security.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // SECURITY: previously allowed all origins (cross-site WebSocket hijacking risk)
                .setAllowedOriginPatterns("http://localhost:3000", "http://localhost:5173", "http://localhost:4200")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // SECURITY: require a valid JWT on the STOMP CONNECT frame
        registration.interceptors(stompAuthChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        // BUG FIX: ChatController delivers private messages via /user/{email}/queue/messages,
        // which the simple broker resolves to /queue/... destinations. "/queue" was not a
        // broker prefix, so delivered messages were silently dropped.
        registry.enableSimpleBroker("/queue", "/topic", "/chat", "/user");
        registry.setUserDestinationPrefix("/user");
    }
}
