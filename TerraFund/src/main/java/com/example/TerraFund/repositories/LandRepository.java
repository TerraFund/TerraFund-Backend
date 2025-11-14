package com.example.TerraFund.repositories;

import com.example.TerraFund.entities.User;
import com.example.TerraFund.model.Land;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LandRepository extends JpaRepository<Land, Long> {
    List<Land> findByOwner(User owner);
    List<Land> findByPublishedTrueAndHiddenFalse();
    List<Land> findByOwnerId(Long ownerId);

    @Query("SELECT l FROM Land l WHERE " +
            "(:region IS NULL OR l.region = :region) AND " +
            "(:type IS NULL OR l.type = :type) AND " +
            "(:waterSource IS NULL OR l.waterSource = :waterSource) AND " +
            "(:soilQuality IS NULL OR l.soilQuality = :soilQuality) AND " +
            "(:minSize IS NULL OR l.size >= :minSize) AND " +
            "(:maxSize IS NULL OR l.size <= :maxSize)")
    List<Land> filterLands(Double minSize, Double maxSize, String region, String type,
                           String waterSource, String soilQuality);
}

