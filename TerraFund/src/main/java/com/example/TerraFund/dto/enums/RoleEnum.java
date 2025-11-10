package com.example.TerraFund.dto.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RoleEnum {
    @JsonProperty("LAND OWNER")
    LAND_OWNER,
    @JsonProperty("INVESTOR")
    INVESTOR,
    @JsonProperty("ADMIN")
    ADMIN
}
