package com.example.TerraFund.repositories;

import com.example.TerraFund.entities.LandProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LandProposalRepository extends JpaRepository <LandProposal, UUID> {
    List<LandProposal> findByInvestorID(Long investorId);
    List<LandProposal> findByLandOwnerID(Long ownerId);
}
