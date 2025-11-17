package com.example.TerraFund.dto.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProposalStatus {
    @JsonProperty("PENDING")
    PENDING,
    @JsonProperty("ACCEPTED")
    ACCEPTED,
    @JsonProperty("REJECTED")
    REJECTED,
    @JsonProperty("CANCELED")
    CANCELED,
    @JsonProperty("WITHDRAWN")
    WITHDRAWN
}
