package com.example.TerraFund.model;

import com.example.TerraFund.entities.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import java.util.List;

@Entity
@Table(name = "lands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Land {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String location;
    private Double size; 
    private String cropSuitability;
    private String ownershipDocPath;
    
    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private boolean verified = false;

    @Builder.Default
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

    List<String> demoImages;

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
