package com.example.TerraFund.dto;

import com.example.TerraFund.dto.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class RegisterRequest {
    String email;
    String password;
    String confirmPassword;
    String firstName;
    String lastName;
    String phoneNumber;
    RoleEnum role;
}
