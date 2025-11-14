package com.example.TerraFund.dto;

import com.example.TerraFund.dto.enums.RoleEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor

public class RegisterRequest {
    @NotBlank(message = "Email is required")
    String email;

    @NotBlank(message = "Password is required")
    String password;

    @NotBlank(message = "Please re-enter the password")
    String confirmPassword;

    @NotBlank(message = "first name is required")
    String firstName;

    @NotBlank(message ="last name is required")
    String lastName;

    @NotBlank(message = "phone number is required")
    String phoneNumber;

    @NotBlank(message = "Choose your role: Investor || LandOwner")
    RoleEnum role;
}
