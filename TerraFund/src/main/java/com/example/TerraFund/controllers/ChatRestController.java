package com.example.TerraFund.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatRestController {

    @GetMapping("/messages")
    public ResponseEntity<?> getChatMessages(){
        return ResponseEntity.ok("Chat messages");
    }
}
