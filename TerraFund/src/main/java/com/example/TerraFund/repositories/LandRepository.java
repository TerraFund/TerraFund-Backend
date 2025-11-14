package com.example.TerraFund.repositories;

import com.example.TerraFund.entities.User;
import com.example.TerraFund.entities.Land;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LandRepository extends JpaRepository<Land, Long> {
    List<Land> findByOwner(User owner);
    List<Land> findByPublishedTrue();
    List<Land> findByOwnerId(Long ownerId);

}

