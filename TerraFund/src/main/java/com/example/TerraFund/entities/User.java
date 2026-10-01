package com.example.TerraFund.entities;

import com.example.TerraFund.dto.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore // never leak password hashes in API responses
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = true)
    @JsonIgnore // never leak OTPs in API responses
    private String otp;

    @Column(nullable = true)
    private Boolean otpVerified = false;

    @Column(nullable = true)
    @JsonIgnore // never leak password-reset tokens in API responses
    private String resetToken;

    @Column(nullable = true)
    @JsonIgnore
    private LocalDateTime resetTokenExpiry;

    @Column(nullable = false)
    private RoleEnum role = RoleEnum.USER;

    @OneToMany(mappedBy = "owner")
    private List<Land> lands;
}
