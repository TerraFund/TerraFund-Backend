package com.example.TerraFund.controllers;

import com.example.TerraFund.entities.Message;
import com.example.TerraFund.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "6. Chat", description = "Chat-related endpoints")
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "Get chat messages", tags = {"6. Chat"})
    @GetMapping("/messages")
    public ResponseEntity<?> getChatMessages(@RequestParam Long user1, @RequestParam Long user2){
        return chatService.getChatMessages(user1, user2);
    }

}
