package com.example.TerraFund.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateLandRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @Positive(message = "Size must be positive")
    private double sizeInHectares;

    private String soilType;

    private Boolean waterSourceIsAvailable = true;

    private Boolean roadAccessIsAvailable = true;

    private String region;

    private String cropSuitability;

    private String waterSource;

    private String soilQuality;

    private Double elevation;

    private Double annualPrice;

    private Boolean published = true;

    private List<String> demoImages;
}
