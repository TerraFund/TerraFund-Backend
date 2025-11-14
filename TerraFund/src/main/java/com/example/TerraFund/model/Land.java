package com.example.TerraFund.model;

import com.example.TerraFund.entities.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Land {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private Double size; // in hectares or acres
    private String cropSuitability;
    private String ownershipDocPath;
    
    @Builder.Default
    private boolean published = false;

    private String soilQuality;
    private String waterSource;
    private Double elevation;
    private String region;
    private String type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
