package com.example.TerraFund.services;

import com.example.TerraFund.dto.LandRequest;
import com.example.TerraFund.model.Land;
import com.example.TerraFund.repositories.LandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LandService {

    private final LandRepository landRepository;

    public Land create(Land land) {
        return landRepository.save(land);
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
        return landRepository.findByPublishedTrue();
    }
}
