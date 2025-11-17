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
import com.example.TerraFund.security.CurrentUser;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.TerraFund.Utils.EmailService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor

public class LandProposalService {
    private  LandRepository landRepository;
    private LandProposalRepository repository;
    private LandProposalRepository landOwnerProfileRepository;
    private CurrentUser currentUser;
    private EmailService emailService;
    private UserRepository userRepository;

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
         proposal.setTitle(request.getTitle());
         proposal.setDescription(request.getDescription());
         proposal.setPurpose(request.getPurpose());
         proposal.setDurationInMonths(request.getDurationInMonths());
         proposal.setStatus(request.getStatus());
         proposal.setAttachments(request.getAttachments());

         repository.save(proposal);
         return ResponseEntity.ok(proposal);

    }

    public ResponseEntity<?> getMyProposals() {
        User user = currentUser.get();

        if (user == null) {
            return ResponseEntity.badRequest().body("You must be logged in to view your proposals!");
        }

        if (user.getRole() != RoleEnum.INVESTOR) {
            return ResponseEntity.badRequest().body("Only investors can have proposals!");
        }

        List<LandProposal> proposals = repository.findByInvestorID(user.getId());

        return ResponseEntity.ok(proposals);
    }

    public ResponseEntity<?> getMyReceivedProposals() {
        User user = currentUser.get();

        if (user == null) {
            return ResponseEntity.badRequest().body("You must be logged in to view received proposals!");
        }

        if (user.getRole() != RoleEnum.LAND_OWNER) {
            return ResponseEntity.badRequest().body("Only land owners can receive proposals!");
        }

        List<LandProposal> proposals = repository.findByLandOwnerID(user.getId());

        return ResponseEntity.ok(proposals);
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

        emailService.sendEmail(investor.toString(),"Your proposal was accepted!", proposal.toString());
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

        emailService.sendEmail(investor.toString(),"Your proposal was rejected!", proposal.toString());

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
