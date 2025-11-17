package com.example.TerraFund.repositories;

import com.example.TerraFund.entities.LandOwnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LandOwnerProfileRepository extends JpaRepository <LandOwnerProfile, Long>{
    Optional<LandOwnerProfile> findByUserEmail(String email);
}
