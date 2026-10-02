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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LandService {

    private final LandRepository landRepository;
    private final CurrentUser currentUser;
    private final FileUploadService fileUploadService;

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
        newLand.setSize(createLandRequest.getSizeInHectares());
        newLand.setSoilType(createLandRequest.getSoilType() != null ? createLandRequest.getSoilType() : "Rich Loam");
        newLand.setWaterSourceIsAvailable(createLandRequest.getWaterSourceIsAvailable() != null ? createLandRequest.getWaterSourceIsAvailable() : true);
        newLand.setRoadAccessIsAvailable(createLandRequest.getRoadAccessIsAvailable() != null ? createLandRequest.getRoadAccessIsAvailable() : true);
        newLand.setRegion(createLandRequest.getRegion() != null ? createLandRequest.getRegion() : "Eastern Province");
        newLand.setCropSuitability(createLandRequest.getCropSuitability());
        newLand.setWaterSource(createLandRequest.getWaterSource());
        newLand.setSoilQuality(createLandRequest.getSoilQuality());
        newLand.setElevation(createLandRequest.getElevation() != null ? createLandRequest.getElevation() : 1500.0);
        newLand.setDemoImages(createLandRequest.getDemoImages());
        newLand.setPublished(createLandRequest.getPublished() != null ? createLandRequest.getPublished() : true);
        newLand.setHidden(false);
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

        if (request.getTitle() != null) land.setTitle(request.getTitle());
        if (request.getDescription() != null) land.setDescription(request.getDescription());
        if (request.getLocation() != null) land.setLocation(request.getLocation());
        if (request.getSizeInHectares() > 0) {
            land.setSizeInHectares(request.getSizeInHectares());
            land.setSize(request.getSizeInHectares());
        }
        if (request.getSoilType() != null) land.setSoilType(request.getSoilType());
        if (request.getWaterSourceIsAvailable() != null) land.setWaterSourceIsAvailable(request.getWaterSourceIsAvailable());
        if (request.getRoadAccessIsAvailable() != null) land.setRoadAccessIsAvailable(request.getRoadAccessIsAvailable());
        if (request.getRegion() != null) land.setRegion(request.getRegion());
        if (request.getCropSuitability() != null) land.setCropSuitability(request.getCropSuitability());
        if (request.getWaterSource() != null) land.setWaterSource(request.getWaterSource());
        if (request.getSoilQuality() != null) land.setSoilQuality(request.getSoilQuality());
        if (request.getElevation() != null) land.setElevation(request.getElevation());
        if (request.getDemoImages() != null) land.setDemoImages(request.getDemoImages());
        if (request.getPublished() != null) land.setPublished(request.getPublished());

        return landRepository.save(land);
    }

    public Land publish(Long id) {
        User user = currentUser.get();
        Land land = getOwnedLand(id, user);
        land.setPublished(true);
        land.setHidden(false);
        return landRepository.save(land);
    }

    public String uploadDocument(Long id, MultipartFile file) {
        User user = currentUser.get();
        Land land = getOwnedLand(id, user);
        try {
            String filename = fileUploadService.saveFile(file);
            land.setOwnershipDocPath("/api/files/download/" + filename);
            landRepository.save(land);
            return filename;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload document", e);
        }
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
