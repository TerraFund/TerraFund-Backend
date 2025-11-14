package com.example.TerraFund.dto;

import lombok.Data;

@Data
public class LandRequest {
    private String title;
    private String location;
    private double size;
    private String cropSuitability;
}
