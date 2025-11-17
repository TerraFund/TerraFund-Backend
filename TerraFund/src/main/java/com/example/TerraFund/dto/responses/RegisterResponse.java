package com.example.TerraFund.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class RegisterResponse {
    private String accessToken;
    private String opt;
}
