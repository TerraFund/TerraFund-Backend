package com.example.TerraFund.services;

import com.example.TerraFund.dto.enums.RoleEnum;
import com.example.TerraFund.dto.requests.LandProposalRequest;
import com.example.TerraFund.entities.LandProposal;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.LandProposalRepository;
import com.example.TerraFund.security.CurrentUser;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor

public class LandProposalService {
    LandProposalRepository repository;
    CurrentUser currentUser;

    public ResponseEntity<?> createNewLandProposal(LandProposalRequest request) {
        User user = currentUser.get();
        if(user == null){
            return ResponseEntity.badRequest().body("You must be logged in to create a proposal!");
        }
         if(user.getRole() != RoleEnum.LAND_OWNER){
             return ResponseEntity.badRequest().body("You must be an investor to create a proposal!");
         }

        LandProposal proposal = new LandProposal();

         proposal.setLandID(request.getLandID());
         proposal.setTitle(request.getTitle());
         proposal.setDescription(request.getDescription());
         proposal.setPurpose(request.getPurpose());
         proposal.setDurationInMonths(request.getDurationInMonths());
         proposal.setStatus(request.getStatus());
         proposal.setAttachments(request.getAttachments());

         return ResponseEntity.ok("Land proposal created successfully!");

    }
}
