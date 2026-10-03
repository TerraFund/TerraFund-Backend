package com.example.TerraFund.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * SECURITY: the verification OTP is no longer part of the register response
 * (it used to let anyone verify any account). It is delivered by email instead.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
public class RegisterResponse {
    private String accessToken;
}
