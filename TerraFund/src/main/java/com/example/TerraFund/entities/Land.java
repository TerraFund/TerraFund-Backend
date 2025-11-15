package com.example.TerraFund.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Land {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String location;

    private Double size;
    private String cropSuitability;
    private String ownershipDocPath;

    private boolean published = false;
    private boolean verified = false;
    private boolean hidden = false;

    private String soilQuality;
    private String waterSource;
    private Double elevation;
    private String region;
    private String type;

    @Column(nullable = false)
    private Double sizeInHectares;

    @Column(nullable = false)
    private String soilType;

    private String title;

    @Column(length = 2000)
    private String description;

    private Boolean waterSourceIsAvailable;
    private Boolean roadAccessIsAvailable;

    @ElementCollection
    private List<String> demoImages;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
