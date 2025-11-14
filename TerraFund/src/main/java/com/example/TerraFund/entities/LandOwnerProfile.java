package com.example.TerraFund.entities;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "land_owner_profiles")
@Getter
@Setter
public class LandOwnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String profilePictureUrl;

    @Column(nullable = false)
    private String nationalIdNumber;

    @Column(nullable = false)
    private String totalLandsListed;


    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

}
