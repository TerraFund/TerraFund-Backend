package com.example.TerraFund.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String timestamp;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long receiverId;
}
