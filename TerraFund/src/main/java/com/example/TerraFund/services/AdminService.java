package com.example.TerraFund.services;

import com.example.TerraFund.entities.Land;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.LandRepository;
import com.example.TerraFund.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final LandRepository landRepository;
    private final UserRepository userRepository;

    public List<Land> findAllLands() {
        return landRepository.findAll();
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Land hideListing(Long id) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found with id: " + id));
        land.setHidden(true);
        return landRepository.save(land);
    }

    public Land unhideListing(Long id) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found with id: " + id));
        land.setHidden(false);
        return landRepository.save(land);
    }

    public Land verifyLand(Long id) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found with id: " + id));
        land.setVerified(true);
        return landRepository.save(land);
    }

    public Land unverifyLand(Long id) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found with id: " + id));
        land.setVerified(false);
        return landRepository.save(land);
    }
}
