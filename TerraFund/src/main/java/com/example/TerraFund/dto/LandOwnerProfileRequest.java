package com.example.TerraFund.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LandOwnerProfileRequest {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String address;

    private String email;

    private String profilePictureUrl;

    private String nationalIdNumber;
}
