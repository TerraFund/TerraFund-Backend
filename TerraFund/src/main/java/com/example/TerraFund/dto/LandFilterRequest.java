package com.example.TerraFund.dto;

import lombok.Data;

@Data
public class LandFilterRequest {
    private double minSize;
    private String location;
}
