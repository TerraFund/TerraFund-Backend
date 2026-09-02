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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class DashboardService {
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final LandProposalRepository proposalRepository;
    private final LandRepository landRepository;

    public ResponseEntity<?> getInvestorDashboard() {
        User user = currentUser.get();
        if (user == null) {
            return ResponseEntity.badRequest().body("You must be logged in to view your dashboard!");
        }

        if (user.getRole() != RoleEnum.INVESTOR && user.getRole() != RoleEnum.ADMIN) {
            return ResponseEntity.badRequest().body("You must be an investor to view this dashboard!");
        }

        List<LandProposal> pendingProposals = proposalRepository.findByInvestorIDAndStatus(user.getId(), ProposalStatus.PENDING);
        List<LandProposal> acceptedProposals = proposalRepository.findByInvestorIDAndStatus(user.getId(), ProposalStatus.ACCEPTED);
        List<LandProposal> rejectedProposals = proposalRepository.findByInvestorIDAndStatus(user.getId(), ProposalStatus.REJECTED);
        List<LandProposal> canceledProposals = proposalRepository.findByInvestorIDAndStatus(user.getId(), ProposalStatus.CANCELED);

        List<Land> availableLands = landRepository.findByPublishedTrueAndHiddenFalse();

        long totalProposals = pendingProposals.size() + acceptedProposals.size() + rejectedProposals.size() + canceledProposals.size();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("pendingProposals", pendingProposals);
        dashboard.put("acceptedProposals", acceptedProposals);
        dashboard.put("rejectedProposals", rejectedProposals);
        dashboard.put("canceledProposals", canceledProposals);
        dashboard.put("availableLands", availableLands);

        dashboard.put("totalProposals", totalProposals);
        dashboard.put("totalPendingProposals", pendingProposals.size());
        dashboard.put("totalAcceptedProposals", acceptedProposals.size());
        dashboard.put("totalRejectedProposals", rejectedProposals.size());
        dashboard.put("totalCanceledProposals", canceledProposals.size());
        dashboard.put("totalAvailableLands", availableLands.size());

        return ResponseEntity.ok(dashboard);
    }

    public ResponseEntity<?> getLandOwnerDashboard() {
        User user = currentUser.get();
        if (user == null) {
            return ResponseEntity.badRequest().body("You must be logged in to view your dashboard!");
        }

        if (user.getRole() != RoleEnum.LAND_OWNER && user.getRole() != RoleEnum.ADMIN) {
            return ResponseEntity.badRequest().body("You must be a land owner to view this dashboard!");
        }

        List<Land> myLands = landRepository.findByOwnerId(user.getId());
        List<LandProposal> receivedProposals = proposalRepository.findByLandOwnerID(user.getId());
        List<LandProposal> pendingProposals = proposalRepository.findByLandOwnerIDAndStatus(user.getId(), ProposalStatus.PENDING);
        List<LandProposal> acceptedProposals = proposalRepository.findByLandOwnerIDAndStatus(user.getId(), ProposalStatus.ACCEPTED);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("myLands", myLands);
        dashboard.put("totalLands", myLands.size());
        dashboard.put("receivedProposals", receivedProposals);
        dashboard.put("totalProposals", receivedProposals.size());
        dashboard.put("pendingProposalsCount", pendingProposals.size());
        dashboard.put("acceptedProposalsCount", acceptedProposals.size());

        return ResponseEntity.ok(dashboard);
    }

    public ResponseEntity<?> getAdminDashboard() {
        User user = currentUser.get();
        if (user == null) {
            return ResponseEntity.badRequest().body("You must be logged in to view the admin dashboard!");
        }

        if (user.getRole() != RoleEnum.ADMIN) {
            return ResponseEntity.badRequest().body("Access denied. Admin role required.");
        }

        List<User> allUsers = userRepository.findAll();
        List<Land> allLands = landRepository.findAll();
        List<LandProposal> allProposals = proposalRepository.findAll();

        long landownerCount = allUsers.stream().filter(u -> u.getRole() == RoleEnum.LAND_OWNER).count();
        long investorCount = allUsers.stream().filter(u -> u.getRole() == RoleEnum.INVESTOR).count();
        long adminCount = allUsers.stream().filter(u -> u.getRole() == RoleEnum.ADMIN).count();
        long userCount = allUsers.stream().filter(u -> u.getRole() == RoleEnum.USER).count();

        long pendingVerifications = allLands.stream().filter(l -> !l.isVerified()).count();
        long activeDeals = allProposals.stream().filter(p -> p.getStatus() == ProposalStatus.ACCEPTED).count();

        Map<String, Object> platformMetrics = new HashMap<>();
        platformMetrics.put("totalUsers", allUsers.size());
        platformMetrics.put("totalLands", allLands.size());
        platformMetrics.put("activeDeals", activeDeals);
        platformMetrics.put("pendingVerifications", pendingVerifications);
        platformMetrics.put("landownerCount", landownerCount);
        platformMetrics.put("investorCount", investorCount);
        platformMetrics.put("adminCount", adminCount);
        platformMetrics.put("userCount", userCount);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("platformMetrics", platformMetrics);
        dashboard.put("users", allUsers);
        dashboard.put("lands", allLands);
        dashboard.put("proposals", allProposals);

        return ResponseEntity.ok(dashboard);
    }
}
