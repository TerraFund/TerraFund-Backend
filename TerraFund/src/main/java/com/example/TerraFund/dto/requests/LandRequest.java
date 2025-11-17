package com.example.TerraFund.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class LandRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Size is required")
    private double sizeInHectares;

    @NotBlank(message = "Soil Quality is required")
    private String soilType;

    @NotBlank(message = "Water Source is required")
    private Boolean waterSourceIsAvailable;

    @NotBlank(message = "Road Access is required")
    private Boolean roadAccessIsAvailable;

    @NotBlank(message = "Demo images are required")
    List<String> demoImages;
}
