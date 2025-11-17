package com.example.TerraFund.dto.requests;

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
