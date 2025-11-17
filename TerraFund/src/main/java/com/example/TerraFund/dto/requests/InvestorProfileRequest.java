package com.example.TerraFund.dto.requests;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class InvestorProfileRequest {

    private String firstName;

    private String lastName;

    private String address;

    private String profilePictureUrl;

    private String nationalIdNumber;

    private String company;

    private String occupation;

    private Long minInvestmentBudget;

    private Long maxInvestmentBudget;
}
