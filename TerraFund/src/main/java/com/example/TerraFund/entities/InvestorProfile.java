package com.example.TerraFund.entities;

import jakarta.persistence.*;

public class InvestorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = true)
    private String profilePictureUrl;

    @Column(nullable = false)
    private String nationalIdNumber;

    @Column(nullable = true)
    private String company;

    @Column(nullable = true)
    private String occupation;

    @Column(nullable = false)
    private Long minInvestmentBudget;

    @Column(nullable = false)
    private Long maxInvestmentBudget;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

}
