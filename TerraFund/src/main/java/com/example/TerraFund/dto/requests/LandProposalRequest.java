package com.example.TerraFund.dto.requests;

import com.example.TerraFund.dto.enums.ProposalStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class LandProposalRequest {
    private Long landID;

    private String title;

    private String description;

    private String purpose;

    private String durationInMonths;

    private Long budget;

    private ProposalStatus status;

    private List<String> attachments;
}
