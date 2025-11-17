package com.example.TerraFund.dto.responses;

import lombok.*;

@Data
@Getter
@AllArgsConstructor
public class InvestorProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private String nationalIdNumber;
    private String company;
    private String occupation;
    private Long minInvestmentBudget;
    private Long maxInvestmentBudget;
}

