package com.example.TerraFund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Email is required")
    String email;

    @NotBlank(message = "Password is required")
    String password;
}
