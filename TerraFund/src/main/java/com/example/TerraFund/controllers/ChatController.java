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

        template.convertAndSendToUser(receiver.getEmail(), "/queue/messages", message);
    }
}
