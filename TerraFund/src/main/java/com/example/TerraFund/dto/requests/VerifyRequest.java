package com.example.TerraFund.dto.requests;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class VerifyRequest {
    private String otp;
}
