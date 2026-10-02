package com.example.TerraFund.dto.responses;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LandOwnerProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String profilePictureUrl;
    private String nationalIdNumber;
}

