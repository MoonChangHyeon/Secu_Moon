package com.example.vulnscanner;

import com.example.vulnscanner.module.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
public class VulnScannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VulnScannerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            userService.createAdminIfNotExists();
        };
    }
}