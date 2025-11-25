package com.example.TerraFund.services;

import com.example.TerraFund.entities.Land;
import com.example.TerraFund.repositories.LandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final LandRepository landRepository;

    public List<Land> findAll() {
        return landRepository.findAll();
    }

    public Land hideListing(Long id) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found with id: " + id));
        // land.setFlagged(true); // assuming you have a 'flagged' boolean field in Land
        return landRepository.save(land);
    }

    public Land verifyLand(Long id) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found with id: " + id));
        land.setVerified(true); // assuming you have a 'verified' boolean field in Land
        return landRepository.save(land);
    }
}

