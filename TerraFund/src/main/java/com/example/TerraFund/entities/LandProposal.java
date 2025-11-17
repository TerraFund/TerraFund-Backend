package com.example.TerraFund.entities;

import com.example.TerraFund.dto.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "land_proposals")
@Getter
@Setter
public class LandProposal {
    @Id
    @GeneratedValue
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private Long landID;

    @Column(nullable = false)
    private Long investorID;

    @Column(nullable = false)
    private Long landOwnerID;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private String durationInMonths;

    @Column(nullable = false)
    private Long budget;

    @Column(nullable = false)
    private ProposalStatus status = ProposalStatus.PENDING;

    @Column
    private List<String> attachments;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
