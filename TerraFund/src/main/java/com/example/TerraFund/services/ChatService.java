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
            return ResponseEntity.status(403).body("You are not authorized to view this chat!");
        }

        List<Message> messages =  messageRepository.findConversation(user1, user2);

        return ResponseEntity.ok(messages);
    }
}
