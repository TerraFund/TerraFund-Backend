package com.example.TerraFund.controllers;

import com.example.TerraFund.dto.enums.RoleEnum;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.security.CurrentUser;
import com.example.TerraFund.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "5. Dashboard", description = "Dashboard-related endpoints")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    @Operation(summary = "Get investor dashboard data")
    @PreAuthorize("hasAnyRole('INVESTOR', 'ADMIN')")
    @GetMapping("/investor")
    public ResponseEntity<?> getInvestorDashboard() {
        return this.dashboardService.getInvestorDashboard();
    }

    @Operation(summary = "Get land owner dashboard data")
    @PreAuthorize("hasAnyRole('LAND_OWNER', 'ADMIN')")
    @GetMapping("/landOwner")
    public ResponseEntity<?> getLandOwnerDashboard() {
        return this.dashboardService.getLandOwnerDashboard();
    }

    @Operation(summary = "Get admin dashboard data")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminDashboard() {
        return this.dashboardService.getAdminDashboard();
    }

    @Operation(summary = "Get user role-specific dashboard data")
    @GetMapping("/me")
    public ResponseEntity<?> getMyDashboard() {
        User user = currentUser.get();
        if (user == null) {
            return ResponseEntity.badRequest().body("Not authenticated");
        }
        if (user.getRole() == RoleEnum.ADMIN) {
            return this.dashboardService.getAdminDashboard();
        } else if (user.getRole() == RoleEnum.LAND_OWNER) {
            return this.dashboardService.getLandOwnerDashboard();
        } else {
            return this.dashboardService.getInvestorDashboard();
        }
    }
}
