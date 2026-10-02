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
    private final com.example.TerraFund.repositories.UserRepository userRepository;

    public ResponseEntity<?> getChatMessages(Long user1, Long user2) {

        if(!currentUser.get().getId().equals(user1) && !currentUser.get().getId().equals(user2)){
            return ResponseEntity.status(403).body("You are not authorized to view this chat!");
        }

        List<Message> messages =  messageRepository.findConversation(user1, user2);

        return ResponseEntity.ok(messages);
    }

    public ResponseEntity<?> sendMessage(Long receiverId, String messageText) {
        com.example.TerraFund.entities.User sender = currentUser.get();
        if (sender == null) {
            return ResponseEntity.badRequest().body("You must be logged in to send a message!");
        }
        com.example.TerraFund.entities.User receiver = userRepository.findById(receiverId).orElse(null);
        if (receiver == null) {
            return ResponseEntity.badRequest().body("Receiver not found");
        }

        Message messageEntity = new Message();
        messageEntity.setMessage(messageText != null ? messageText : "");
        messageEntity.setSenderId(sender.getId());
        messageEntity.setReceiverId(receiver.getId());
        messageEntity.setTimestamp(java.time.LocalDateTime.now().toString());

        messageRepository.save(messageEntity);
        return ResponseEntity.ok(messageEntity);
    }
}
