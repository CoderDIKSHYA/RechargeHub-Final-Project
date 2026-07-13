package com.capg.RechargeHub.config;

import com.capg.RechargeHub.entity.User;
import com.capg.RechargeHub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminDataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "runwalvlogging@gmail.com";
        Optional<User> adminOpt = userRepository.findByEmail(adminEmail);

        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            logger.info("Admin user {} found. Resetting password to 'password123' for consistency.", adminEmail);
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setRole("ROLE_ADMIN");
            admin.setVerified(true);
            userRepository.save(admin);
        } else {
            logger.info("Admin user {} not found. Creating new admin account.", adminEmail);
            User admin = new User();
            admin.setName("Admin Dikshya");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setRole("ROLE_ADMIN");
            admin.setVerified(true);
            userRepository.save(admin);
            logger.info("Admin user {} created successfully.", adminEmail);
        }
    }
}
