package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.LandProposalRequest;
import com.example.TerraFund.services.LandProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "4. Land Proposal", description = "Land Proposal-related endpoints")
@RequestMapping("/land-proposal")
@PreAuthorize("hasRole('INVESTOR')")
public class LandProposalController {
    LandProposalService service;

    @Operation(summary = "Create new land proposal", tags = {"4. Land Proposal"})
    @PostMapping("/create")
    public ResponseEntity<?> createNewLandProposal(@RequestBody LandProposalRequest request) {
        return this.service.createNewLandProposal(request);
    }

    @Operation(summary = "Get all my land proposals", tags = {"4. Land Proposal"})
    @GetMapping("/my-proposals")
    public ResponseEntity<?> getAllMyLandProposals() {
        return this.service.getMyProposals();
    }

    @Operation(summary = "Get all my received land proposals", tags = {"4. Land Proposal"})
    @GetMapping("/my-received-proposals")
    public ResponseEntity<?> getAllMyReceivedLandProposals() {
        return this.service.getMyReceivedProposals();
    }

    @Operation(summary = "Accept land proposal", tags = {"4. Land Proposal"})
    @PatchMapping("/accept/:id")
    public ResponseEntity<?> acceptLandProposal(@PathVariable UUID id) {
        return this.service.acceptLandProposal(id);
    }

}
