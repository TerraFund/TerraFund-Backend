package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.MessageDto;
import com.example.TerraFund.entities.Message;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.MessageRepository;
import com.example.TerraFund.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class ChatController {
    private final SimpMessagingTemplate template;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * SECURITY: the sender identity is no longer taken from the client-supplied
     * payload (which allowed impersonation of any user). It is derived from the
     * authenticated WebSocket principal (see StompAuthChannelInterceptor).
     * Delivery uses the receiver's principal name, so users can only receive
     * messages addressed to their own session.
     */
    /**
     * Destinations: /app/chat.private (plus un-prefixed client variants).
     * BUG FIX: the original "app/chat.private" declaration doubled the /app
     * prefix, so client messages were never routed here.
     */
    @MessageMapping({"/chat.private", "chat.private", "app/chat.private"})
    public void sendMessage(MessageDto message, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("Unauthenticated WebSocket user");
        }

        User sender = userRepository.findByEmail(principal.getName()).orElse(null);
        if (sender == null) {
            throw new IllegalArgumentException("Unknown sender");
        }

        User receiver = userRepository.findById(message.getReceiverId()).orElse(null);
        if (receiver == null) {
            throw new IllegalArgumentException("Unknown receiver");
        }

        Message messageEntity = new Message();
        messageEntity.setMessage(message.getMessage());
        messageEntity.setSenderId(sender.getId()); // enforced server-side
        messageEntity.setReceiverId(receiver.getId());
        messageEntity.setTimestamp(message.getTimestamp());

        messageRepository.save(messageEntity);

        // SECURITY: push a sanitized copy - the client-supplied senderId must not
        // be echoed to the receiver (previously the pushed message could show a
        // spoofed sender even though the stored record had the real one).
        MessageDto outgoing = new MessageDto();
        outgoing.setSenderId(sender.getId());
        outgoing.setReceiverId(receiver.getId());
        outgoing.setMessage(message.getMessage());
        outgoing.setTimestamp(message.getTimestamp());

        template.convertAndSendToUser(receiver.getEmail(), "/queue/messages", outgoing);
    }
}
