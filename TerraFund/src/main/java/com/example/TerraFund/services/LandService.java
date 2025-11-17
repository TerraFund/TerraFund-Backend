package com.example.TerraFund.services;

import com.example.TerraFund.dto.requests.CreateLandRequest;
import com.example.TerraFund.entities.Land;
import com.example.TerraFund.repositories.LandRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LandService {

    private final LandRepository landRepository;

    public Land create(CreateLandRequest createLandRequest) {
        Land newLand = new Land();
        newLand.setTitle(createLandRequest.getTitle());
        newLand.setDescription(createLandRequest.getDescription());
        newLand.setLocation(createLandRequest.getLocation());
        newLand.setSizeInHectares(createLandRequest.getSizeInHectares());
        newLand.setSoilType(createLandRequest.getSoilType());
        newLand.setWaterSourceIsAvailable(createLandRequest.getWaterSourceIsAvailable());
        newLand.setRoadAccessIsAvailable(createLandRequest.getRoadAccessIsAvailable());
        return landRepository.save(newLand);
    }

    public Optional<Land> findById(Long id) {
        return landRepository.findById(id);
    }

    public List<Land> findByOwner(Long ownerId) {
        return landRepository.findByOwnerId(ownerId);
    }

    public Land update(Land land) {
        return landRepository.save(land);
    }

    public void delete(Long id) {
        landRepository.deleteById(id);
    }

    public List<Land> listPublished() {
        return landRepository.findByPublishedTrueAndHiddenFalse();
    }

    public List<Land> findAll() {
        return landRepository.findAll();
    }

    public Land hideListing(Long landId) {
        Land land = landRepository.findById(landId)
                .orElseThrow(() -> new EntityNotFoundException("Land not found with id " + landId));
        land.setHidden(true);
        land.setPublished(false);
        return landRepository.save(land);
    }

    public Land verifyLand(Long landId) {
        Land land = landRepository.findById(landId)
                .orElseThrow(() -> new EntityNotFoundException("Land not found with id " + landId));
        land.setVerified(true);
        land.setHidden(false);
        return landRepository.save(land);
    }
}
