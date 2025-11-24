package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.MessageDto;
import com.example.TerraFund.entities.Message;
import com.example.TerraFund.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {
    private final SimpMessagingTemplate template;
    private final MessageRepository messageRepository;

    @MessageMapping("app/chat.private")
    public void sendMessage(MessageDto message) {

        Message messageEntity = new Message();
        messageEntity.setMessage(message.getMessage());
        messageEntity.setSenderId(message.getSenderId());
        messageEntity.setReceiverId(message.getReceiverId());
        messageEntity.setTimestamp(message.getTimestamp());

        messageRepository.save(messageEntity);
        template.convertAndSendToUser(message.getReceiverId().toString(), "/queue/messages", message);
    }
}
