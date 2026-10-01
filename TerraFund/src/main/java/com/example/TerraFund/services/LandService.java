package com.example.TerraFund.services;

import com.example.TerraFund.dto.requests.CreateLandRequest;
import com.example.TerraFund.entities.Land;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.LandRepository;
import com.example.TerraFund.security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LandService {

    private final LandRepository landRepository;
    private final CurrentUser currentUser;

    /**
     * SECURITY: the owner is now set from the authenticated user. Previously lands
     * were created with a null owner, breaking ownership checks entirely.
     */
    public Land create(CreateLandRequest createLandRequest) {
        User owner = currentUser.get();
        Land newLand = new Land();
        newLand.setTitle(createLandRequest.getTitle());
        newLand.setDescription(createLandRequest.getDescription());
        newLand.setLocation(createLandRequest.getLocation());
        newLand.setSizeInHectares(createLandRequest.getSizeInHectares());
        newLand.setSoilType(createLandRequest.getSoilType());
        newLand.setWaterSourceIsAvailable(createLandRequest.getWaterSourceIsAvailable());
        newLand.setRoadAccessIsAvailable(createLandRequest.getRoadAccessIsAvailable());
        newLand.setOwner(owner);
        return landRepository.save(newLand);
    }

    public Optional<Land> findById(Long id) {
        return landRepository.findById(id);
    }

    public List<Land> findByOwner(Long ownerId) {
        return landRepository.findByOwnerId(ownerId);
    }

    /**
     * SECURITY: verifies the authenticated user owns the land, then copies only
     * the editable fields. Previously any land owner could update ANY land
     * (IDOR) and could mass-assign protected fields such as owner, verified,
     * published and hidden by posting a full Land entity.
     */
    public Land update(Long id, CreateLandRequest request) {
        User user = currentUser.get();
        Land land = getOwnedLand(id, user);

        land.setTitle(request.getTitle());
        land.setDescription(request.getDescription());
        land.setLocation(request.getLocation());
        land.setSizeInHectares(request.getSizeInHectares());
        land.setSoilType(request.getSoilType());
        land.setWaterSourceIsAvailable(request.getWaterSourceIsAvailable());
        land.setRoadAccessIsAvailable(request.getRoadAccessIsAvailable());

        return landRepository.save(land);
    }

    /**
     * SECURITY: only the owner can delete their land (previously IDOR).
     */
    public void delete(Long id) {
        User user = currentUser.get();
        Land land = getOwnedLand(id, user);
        landRepository.delete(land);
    }

    private Land getOwnedLand(Long id, User user) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Land not found"));
        if (land.getOwner() == null || !land.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this land");
        }
        return land;
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
