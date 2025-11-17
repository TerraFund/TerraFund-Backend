package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.requests.LandProposalRequest;
import com.example.TerraFund.services.LandProposalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/landproposal")
@PreAuthorize("hasRole('INVESTOR')")
public class LandProposalController {
    LandProposalService service;

    public ResponseEntity<?> createNewLandProposal(@RequestBody LandProposalRequest request) {
        return this.service.createNewLandProposal(request);
    }
}
