package com.example.TerraFund.config;

import com.example.TerraFund.dto.enums.RoleEnum;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "geofreykayin@gmail.com";
        String adminPassword = "geo654@!";

        User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setPhoneNumber("+250788000000");
        admin.setOtpVerified(true);
        admin.setRole(RoleEnum.ADMIN);

        userRepository.save(admin);
        log.info("Admin user seeded successfully with email: {}", adminEmail);
    }
}
