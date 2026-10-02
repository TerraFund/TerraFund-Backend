package com.example.TerraFund.services;

import com.example.TerraFund.dto.enums.ProposalStatus;
import com.example.TerraFund.dto.enums.RoleEnum;
import com.example.TerraFund.dto.requests.LandProposalRequest;
import com.example.TerraFund.entities.Land;
import com.example.TerraFund.entities.LandProposal;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.LandProposalRepository;
import com.example.TerraFund.repositories.LandRepository;
import com.example.TerraFund.repositories.UserRepository;
import com.example.TerraFund.repositories.LandOwnerProfileRepository;
import com.example.TerraFund.repositories.InvestorProfileRepository;
import com.example.TerraFund.security.CurrentUser;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.TerraFund.Utils.EmailService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class LandProposalService {
    private final LandRepository landRepository;
    private final LandProposalRepository repository;
    private final CurrentUser currentUser;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final LandOwnerProfileRepository landOwnerProfileRepository;
    private final InvestorProfileRepository investorProfileRepository;

    public ResponseEntity<?> createNewLandProposal(LandProposalRequest request) {
        User user = currentUser.get();
        if(user == null){
            return ResponseEntity.badRequest().body("You must be logged in to create a proposal!");
        }
         if(user.getRole() != RoleEnum.INVESTOR){
             return ResponseEntity.badRequest().body("You must be an investor to create a proposal!");
         }

        Land land = landRepository.findById(request.getLandID())
                .orElseThrow(() -> new RuntimeException("Land not found"));

        User owner =  land.getOwner();

        LandProposal proposal = new LandProposal();

        proposal.setInvestorID(user.getId());
        proposal.setLandOwnerID(owner.getId());
        proposal.setLandID(request.getLandID());
        proposal.setTitle(request.getTitle() != null ? request.getTitle() : "Investment Proposal");
        proposal.setDescription(request.getDescription() != null ? request.getDescription() : "");
        proposal.setPurpose(request.getPurpose() != null ? request.getPurpose() : "Agricultural Investment");
        proposal.setDurationInMonths(request.getDurationInMonths() != null ? String.valueOf(request.getDurationInMonths()) : "12");
        proposal.setBudget(request.getBudget() != null ? request.getBudget() : 10000L);
        // SECURITY: status is server-controlled; previously a client could create
        // a proposal already marked ACCEPTED.
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setAttachments(request.getAttachments());
        proposal.setCreatedOn(java.time.LocalDateTime.now());
        proposal.setUpdatedOn(java.time.LocalDateTime.now());

        repository.save(proposal);
        return ResponseEntity.ok(proposal);

    }

    private java.util.Map<String, Object> enrichProposal(LandProposal proposal) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", proposal.getId());
        map.put("landID", proposal.getLandID());
        map.put("landId", proposal.getLandID());
        map.put("investorID", proposal.getInvestorID());
        map.put("landOwnerID", proposal.getLandOwnerID());
        map.put("title", proposal.getTitle());
        map.put("description", proposal.getDescription());
        map.put("notes", proposal.getDescription());
        map.put("message", proposal.getDescription() != null && !proposal.getDescription().isBlank() ? proposal.getDescription() : proposal.getTitle());
        map.put("purpose", proposal.getPurpose());
        map.put("intendedCrop", proposal.getPurpose());
        map.put("durationInMonths", proposal.getDurationInMonths());
        map.put("duration", proposal.getDurationInMonths());
        map.put("budget", proposal.getBudget());
        map.put("amount", proposal.getBudget());
        map.put("proposedAmount", "$" + (proposal.getBudget() != null ? String.format("%,d", proposal.getBudget()) : "0"));
        map.put("status", proposal.getStatus() != null ? proposal.getStatus().name().toLowerCase() : "pending");
        map.put("statusUpper", proposal.getStatus() != null ? proposal.getStatus().name() : "PENDING");
        map.put("attachments", proposal.getAttachments());
        map.put("createdOn", proposal.getCreatedOn());
        map.put("createdAt", proposal.getCreatedOn() != null ? proposal.getCreatedOn().toString().substring(0, 10) : "");
        map.put("created_at", proposal.getCreatedOn());
        map.put("updatedOn", proposal.getUpdatedOn());

        landRepository.findById(proposal.getLandID()).ifPresent(land -> {
            map.put("landTitle", land.getTitle());
            map.put("landLocation", land.getLocation());
            map.put("landSize", land.getSizeInHectares());
        });

        userRepository.findById(proposal.getInvestorID()).ifPresent(inv -> {
            map.put("investorEmail", inv.getEmail());
            map.put("investorPhone", inv.getPhoneNumber());
            investorProfileRepository.findByUserEmail(inv.getEmail()).ifPresentOrElse(prof -> {
                String fName = prof.getFirstName() != null ? prof.getFirstName() : "";
                String lName = prof.getLastName() != null ? prof.getLastName() : "";
                map.put("investorName", (fName + " " + lName).trim());
            }, () -> {
                map.put("investorName", inv.getEmail().split("@")[0]);
            });
        });

        userRepository.findById(proposal.getLandOwnerID()).ifPresent(owner -> {
            map.put("landownerEmail", owner.getEmail());
            map.put("landownerPhone", owner.getPhoneNumber());
            landOwnerProfileRepository.findByUserEmail(owner.getEmail()).ifPresentOrElse(prof -> {
                String fName = prof.getFirstName() != null ? prof.getFirstName() : "";
                String lName = prof.getLastName() != null ? prof.getLastName() : "";
                map.put("landownerName", (fName + " " + lName).trim());
            }, () -> {
                map.put("landownerName", owner.getEmail().split("@")[0]);
            });
        });

        return map;
    }

    public ResponseEntity<?> getLandProposalById(UUID id) {
        LandProposal proposal = repository.findById(id).orElse(null);
        if (proposal == null) {
            return ResponseEntity.notFound().build();
        }

        User user = currentUser.get();
        if (user != null && user.getRole() != RoleEnum.ADMIN) {
            if (!Objects.equals(user.getId(), proposal.getInvestorID()) &&
                !Objects.equals(user.getId(), proposal.getLandOwnerID())) {
                return ResponseEntity.status(403).body("Access denied");
            }
        }

        return ResponseEntity.ok(enrichProposal(proposal));
    }

    public ResponseEntity<?> getMyProposals() {
        User user = currentUser.get();

        if (user == null) {
            return ResponseEntity.badRequest().body("You must be logged in to view your proposals!");
        }

        if (user.getRole() != RoleEnum.INVESTOR && user.getRole() != RoleEnum.ADMIN) {
            return ResponseEntity.badRequest().body("Only investors can have proposals!");
        }

        List<LandProposal> proposals = repository.findByInvestorID(user.getId());
        List<java.util.Map<String, Object>> result = proposals.stream().map(this::enrichProposal).toList();

        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> getMyReceivedProposals() {
        User user = currentUser.get();

        if (user == null) {
            return ResponseEntity.badRequest().body("You must be logged in to view received proposals!");
        }

        if (user.getRole() != RoleEnum.LAND_OWNER && user.getRole() != RoleEnum.ADMIN) {
            return ResponseEntity.badRequest().body("Only land owners can receive proposals!");
        }

        List<LandProposal> proposals = repository.findByLandOwnerID(user.getId());
        List<java.util.Map<String, Object>> result = proposals.stream().map(this::enrichProposal).toList();

        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> acceptLandProposal(UUID id) {
        LandProposal proposal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land Proposal not found"));

        User user = currentUser.get();

        if (user.getRole() != RoleEnum.LAND_OWNER) {
            return ResponseEntity.badRequest().body("Only land owners can accept proposals!");
        }

        if (!Objects.equals(user.getId(), proposal.getLandOwnerID())) {
            return ResponseEntity.badRequest().body("You cannot accept a proposal for another land owner!");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            return ResponseEntity.badRequest().body("Proposal is not in pending status!");
        }

        User investor = userRepository.findById(proposal.getInvestorID())
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        // BUG FIX: recipient was investor.toString(), which is not an email address
        emailService.sendEmail(investor.getEmail(),"Your proposal was accepted!", proposal.toString());
        proposal.setStatus(ProposalStatus.ACCEPTED);
        repository.save(proposal);
        return ResponseEntity.ok(proposal);
    }

    public ResponseEntity<?> rejectLandProposal(UUID id) {
        LandProposal proposal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land Proposal not found"));

        User user = currentUser.get();

        if (user.getRole() != RoleEnum.LAND_OWNER) {
            return ResponseEntity.badRequest().body("Only land owners can reject proposals!");
        }

        if (!Objects.equals(user.getId(), proposal.getLandOwnerID())) {
            return ResponseEntity.badRequest().body("You cannot reject a proposal for another land owner!");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            return ResponseEntity.badRequest().body("Proposal is not in pending status!");
        }

        proposal.setStatus(ProposalStatus.REJECTED);
        repository.save(proposal);

        User investor = userRepository.findById(proposal.getInvestorID())
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        emailService.sendEmail(investor.getEmail(),"Your proposal was rejected!", proposal.toString());

        return ResponseEntity.ok(proposal);
    }

    public ResponseEntity<?> cancelLandProposal(UUID id) {
        LandProposal proposal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land Proposal not found"));

        User user = currentUser.get();

        if (user.getRole() != RoleEnum.INVESTOR) {
            return ResponseEntity.badRequest().body("Only investors can cancel proposals!");
        }

        if (!Objects.equals(user.getId(), proposal.getInvestorID())) {
            return ResponseEntity.badRequest().body("You cannot cancel someone else's proposal!");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            return ResponseEntity.badRequest().body("Only pending proposals can be cancelled!");
        }

        proposal.setStatus(ProposalStatus.CANCELED);
        repository.save(proposal);

        User owner = userRepository.findById(proposal.getLandOwnerID())
                .orElseThrow(() -> new RuntimeException("Land owner not found"));

        return ResponseEntity.ok(proposal);
    }

}
