package com.example.TerraFund.services;

import com.example.TerraFund.entities.Message;
import com.example.TerraFund.repositories.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private final MessageRepository messageRepository;

    public ChatService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public ResponseEntity<?> getChatMessages(Long user1, Long user2) {
        List<Message> messages =  messageRepository.findConversation(user1, user2);
        return ResponseEntity.ok(messages);
    }
}
