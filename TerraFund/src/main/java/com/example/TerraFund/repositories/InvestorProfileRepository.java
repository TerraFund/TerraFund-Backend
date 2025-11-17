package com.example.TerraFund.repositories;

import com.example.TerraFund.entities.InvestorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestorProfileRepository extends JpaRepository<InvestorProfile, Long> {
    Optional<InvestorProfile> findByUserEmail(String email);
}
