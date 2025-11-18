package com.example.TerraFund.controllers;

import com.example.TerraFund.services.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/dashboard")
@Tag(name = "5. Dashboard", description = "Dashboard-related endpoints")
public class DashboardController {

    private DashboardService dashboardService;

    @PreAuthorize("hasRole('INVESTOR')")
    @GetMapping("/investor")
    public ResponseEntity<?> getInvestorDashboard() {
        return this.dashboardService.getInvestorDashboard();
    }

    @GetMapping("/landOwner")
    public ResponseEntity<?> getLandOwnerDashboard() {
        return this.dashboardService.getLandOwnerDashboard();
    }

}
