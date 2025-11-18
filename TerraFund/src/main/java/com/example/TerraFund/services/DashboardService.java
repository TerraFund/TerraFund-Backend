package com.example.TerraFund.services;

import com.example.TerraFund.dto.enums.ProposalStatus;
import com.example.TerraFund.dto.enums.RoleEnum;
import com.example.TerraFund.entities.Land;
import com.example.TerraFund.entities.LandProposal;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.LandProposalRepository;
import com.example.TerraFund.repositories.LandRepository;
import com.example.TerraFund.repositories.UserRepository;
import com.example.TerraFund.security.CurrentUser;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor

public class DashboardService {
    private CurrentUser currentUser;
    private UserRepository userRepository;
    private LandProposalRepository proposalRepository;
    private LandRepository landRepository;

    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<?> getInvestorDashboard(){
        User user = currentUser.get();
        if(user == null){
            return ResponseEntity.badRequest().body("You must be logged in to view your dashboard!");
        }

        if(user.getRole() != RoleEnum.INVESTOR){
            return ResponseEntity.badRequest().body("You must be an investor to view your dashboard!");
        }

        List<LandProposal> acceptedProposals = proposalRepository.findByInvestorIDAndStatus(user.getId(), ProposalStatus.PENDING);
        List<LandProposal> rejectedProposals = proposalRepository.findByInvestorIDAndStatus(user.getId(), ProposalStatus.ACCEPTED);
        List<LandProposal> canceledProposals = proposalRepository.findByInvestorIDAndStatus(user.getId(), ProposalStatus.REJECTED);

        List<Land> availableLands = landRepository.findAll();

        long totalProposals = acceptedProposals.size() + rejectedProposals.size() + canceledProposals.size() + completedProposals.size();
        long totalAcceptedProposals = acceptedProposals.size();
        long totalRejectedProposals = rejectedProposals.size();
        long totalCanceledProposals = canceledProposals.size();

        long totalAvailableLands = availableLands.size();


    }

    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<?> getLandOwnerDashboard(){
        return null;
    }

    public ResponseEntity<?> getAdminDashboard(){
        return null;
    }
}
