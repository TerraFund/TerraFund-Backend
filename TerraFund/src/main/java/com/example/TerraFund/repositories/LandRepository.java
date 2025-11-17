package com.example.TerraFund.repositories;

import com.example.TerraFund.entities.User;
import com.example.TerraFund.entities.Land;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LandRepository extends JpaRepository<Land, Long> {
    List<Land> findByPublishedTrueAndHiddenFalse();
    List<Land> findByOwnerId(Long ownerId);
}

