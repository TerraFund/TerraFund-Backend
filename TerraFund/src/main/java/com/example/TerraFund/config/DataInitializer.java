package com.example.TerraFund.config;

import com.example.TerraFund.dto.enums.RoleEnum;
import com.example.TerraFund.entities.InvestorProfile;
import com.example.TerraFund.entities.Land;
import com.example.TerraFund.entities.LandOwnerProfile;
import com.example.TerraFund.entities.User;
import com.example.TerraFund.repositories.InvestorProfileRepository;
import com.example.TerraFund.repositories.LandOwnerProfileRepository;
import com.example.TerraFund.repositories.LandRepository;
import com.example.TerraFund.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LandRepository landRepository;
    private final LandOwnerProfileRepository landOwnerProfileRepository;
    private final InvestorProfileRepository investorProfileRepository;

    @Override
    public void run(String... args) {
        seedUsers();
        seedLands();
    }

    private void seedUsers() {
        // 1. Admin
        String adminEmail = System.getenv().getOrDefault("ADMIN_EMAIL", "geofreykayin@gmail.com");
        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin12345");
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPhoneNumber("+250788000000");
            admin.setOtpVerified(true);
            admin.setRole(RoleEnum.ADMIN);
            userRepository.save(admin);
            log.info("Admin user seeded: {}", adminEmail);
        }

        // 2. Land Owner
        String landownerEmail = "landowner@terrafund.org";
        if (!userRepository.existsByEmail(landownerEmail)) {
            User landowner = new User();
            landowner.setEmail(landownerEmail);
            landowner.setPassword(passwordEncoder.encode("password123"));
            landowner.setPhoneNumber("+250788111222");
            landowner.setOtpVerified(true);
            landowner.setRole(RoleEnum.LAND_OWNER);
            userRepository.save(landowner);

            LandOwnerProfile profile = new LandOwnerProfile();
            profile.setFirstName("Geofrey");
            profile.setLastName("Kayin");
            profile.setEmail(landownerEmail);
            profile.setPhoneNumber("+250788111222");
            profile.setAddress("Kigali, Rwanda");
            profile.setNationalIdNumber("1199080012345678");
            profile.setTotalLandsListed("3");
            profile.setProfilePictureUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80");
            profile.setUser(landowner);
            landOwnerProfileRepository.save(profile);
            log.info("Landowner user and profile seeded: {}", landownerEmail);
        }

        // 3. Investor
        String investorEmail = "investor@terrafund.org";
        if (!userRepository.existsByEmail(investorEmail)) {
            User investor = new User();
            investor.setEmail(investorEmail);
            investor.setPassword(passwordEncoder.encode("password123"));
            investor.setPhoneNumber("+250788333444");
            investor.setOtpVerified(true);
            investor.setRole(RoleEnum.INVESTOR);
            userRepository.save(investor);

            InvestorProfile profile = new InvestorProfile();
            profile.setFirstName("Sarah");
            profile.setLastName("Johnson");
            profile.setEmail(investorEmail);
            profile.setPhoneNumber("+250788333444");
            profile.setAddress("Nairobi, Kenya");
            profile.setNationalIdNumber("KEN-99887766");
            profile.setCompany("AgroFund East Africa");
            profile.setOccupation("Principal Agronomist & Fund Manager");
            profile.setMinInvestmentBudget(10000L);
            profile.setMaxInvestmentBudget(250000L);
            profile.setProfilePictureUrl("https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80");
            profile.setUser(investor);
            investorProfileRepository.save(profile);
            log.info("Investor user and profile seeded: {}", investorEmail);
        }
    }

    private void seedLands() {
        if (landRepository.count() > 0) return;

        User owner = userRepository.findByEmail("landowner@terrafund.org")
                .or(() -> userRepository.findByEmail("geofreykayin@gmail.com"))
                .orElse(null);

        if (owner == null) return;

        Land land1 = new Land();
        land1.setTitle("Highland Organic Coffee Estate");
        land1.setLocation("Huye District, Southern Province");
        land1.setRegion("Southern Province");
        land1.setSizeInHectares(45.5);
        land1.setSize(45.5);
        land1.setSoilType("Volcanic Loam (pH 6.2)");
        land1.setWaterSource("Natural Spring & Solar Drip Irrigation");
        land1.setWaterSourceIsAvailable(true);
        land1.setRoadAccessIsAvailable(true);
        land1.setCropSuitability("Arabica Coffee, Macadamia, Highland Tea");
        land1.setElevation(1750.0);
        land1.setDescription("Prime agricultural highland estate with rich volcanic soil, optimal altitude for Arabica coffee, and integrated drip irrigation infrastructure.");
        land1.setPublished(true);
        land1.setVerified(true);
        land1.setHidden(false);
        land1.setOwner(owner);
        land1.setDemoImages(List.of("https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1000&q=80"));

        Land land2 = new Land();
        land2.setTitle("Rift Valley Commercial Maize & Soybean Plot");
        land2.setLocation("Nyagatare, Eastern Province");
        land2.setRegion("Eastern Province");
        land2.setSizeInHectares(120.0);
        land2.setSize(120.0);
        land2.setSoilType("Clay Loam (pH 6.8)");
        land2.setWaterSource("River Canal & Center Pivot");
        land2.setWaterSourceIsAvailable(true);
        land2.setRoadAccessIsAvailable(true);
        land2.setCropSuitability("Hybrid Maize, Soybean, Sunflower");
        land2.setElevation(1350.0);
        land2.setDescription("Flat, high-yield mechanized farming terrain with direct river canal access and pivot irrigation suitability.");
        land2.setPublished(true);
        land2.setVerified(true);
        land2.setHidden(false);
        land2.setOwner(owner);
        land2.setDemoImages(List.of("https://images.unsplash.com/photo-1592982537447-7440770cbfc9?auto=format&fit=crop&w=1000&q=80"));

        Land land3 = new Land();
        land3.setTitle("Musanze Premium Avocado & Horticulture Valley");
        land3.setLocation("Musanze, Northern Province");
        land3.setRegion("Northern Province");
        land3.setSizeInHectares(28.0);
        land3.setSize(28.0);
        land3.setSoilType("Volcanic Soil (pH 6.4)");
        land3.setWaterSource("Borehole & Rain Catchment");
        land3.setWaterSourceIsAvailable(true);
        land3.setRoadAccessIsAvailable(true);
        land3.setCropSuitability("Hass Avocado, French Beans, Snow Peas");
        land3.setElevation(1850.0);
        land3.setDescription("Export-grade horticulture land equipped with cold-chain storage access points and high organic matter content.");
        land3.setPublished(true);
        land3.setVerified(false);
        land3.setHidden(false);
        land3.setOwner(owner);
        land3.setDemoImages(List.of("https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=1000&q=80"));

        landRepository.saveAll(Arrays.asList(land1, land2, land3));
        log.info("Demo agricultural lands seeded successfully");
    }
}
