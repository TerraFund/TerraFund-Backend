package com.example.TerraFund.dto.requests;

import lombok.Data;

@Data
public class LandFilterRequest {
    private double minSize;
    private String location;
}
