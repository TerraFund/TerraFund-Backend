package com.example.TerraFund.config;

import com.example.TerraFund.dto.enums.RoleEnum;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Seeds the initial ADMIN account on startup.
 *
 * SECURITY: the admin credentials are no longer hardcoded (the previous version
 * committed a real password to git). Configure them via environment variables;
 * if ADMIN_PASSWORD is not set, a random password is generated and printed once
 * to the application log so it can be retrieved and changed on first login.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = System.getenv("ADMIN_EMAIL");
        String adminPassword = System.getenv("ADMIN_PASSWORD");

        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("ADMIN_EMAIL not set - skipping admin seeding");
            return;
        }

        boolean generated = false;
        if (adminPassword == null || adminPassword.isBlank()) {
            adminPassword = randomPassword();
            generated = true;
        }

        User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setPhoneNumber(System.getenv().getOrDefault("ADMIN_PHONE", "+250000000000"));
        admin.setOtpVerified(true);
        admin.setRole(RoleEnum.ADMIN);

        userRepository.save(admin);

        if (generated) {
            log.info("Admin user seeded for {} with a RANDOMLY GENERATED password (printed once, change it after first login): {}",
                    adminEmail, adminPassword);
        } else {
            log.info("Admin user seeded successfully with email: {}", adminEmail);
        }
    }

    private String randomPassword() {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
