package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.LandProposalRequest;
import com.example.TerraFund.services.LandProposalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/land-proposal")
@PreAuthorize("hasRole('INVESTOR')")
public class LandProposalController {
    LandProposalService service;

    @PostMapping("/create")
    public ResponseEntity<?> createNewLandProposal(@RequestBody LandProposalRequest request) {
        return this.service.createNewLandProposal(request);
    }

    @GetMapping("/my-proposals")
    public ResponseEntity<?> getAllMyLandProposals() {
        return this.service.getMyProposals();
    }

    @GetMapping("/my-received-proposals")
    public ResponseEntity<?> getAllMyReceivedLandProposals() {
        return this.service.getMyReceivedProposals();
    }

    @PatchMapping("/accept/:id")
    public ResponseEntity<?> acceptLandProposal(@PathVariable UUID id) {
        return this.service.acceptLandProposal(id);
    }

}
