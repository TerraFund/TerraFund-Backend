package com.example.TerraFund.services;

import com.example.TerraFund.entities.Message;
import com.example.TerraFund.repositories.MessageRepository;
import com.example.TerraFund.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MessageRepository messageRepository;
    private final CurrentUser currentUser;

    public ResponseEntity<?> getChatMessages(Long user1, Long user2) {

        if(!currentUser.get().getId().equals(user1) && !currentUser.get().getId().equals(user2)){
            return ResponseEntity.badRequest().body("You must be logged in as either user1 or user2 to view chat messages!");
        }

        List<Message> messages =  messageRepository.findConversation(user1, user2);
        return ResponseEntity.ok(messages);
    }
}
