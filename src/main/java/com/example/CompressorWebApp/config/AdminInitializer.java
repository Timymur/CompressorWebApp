package com.example.CompressorWebApp.config;

import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.login}")
    private String defaultLogin;

    @Value("${admin.default.password}")
    private String defaultPassword;

    public AdminInitializer(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if (userService.countByRole("admin") == 0) {
            User admin = new User();
            admin.setLogin(defaultLogin);
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            admin.setRole("admin");
            admin.setFirstName("Администратор");
            admin.setLastName("Администратор");
            admin.setJobTitle("Администратор");
            admin.setStation(null);
            admin.setInWork(false);

            userService.save(admin);

        }
    }
}