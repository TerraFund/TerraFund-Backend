package com.example.TerraFund.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MessageDto {
    private Long senderId;
    private Long receiverId;
    private String message;
    private String timestamp;
}
