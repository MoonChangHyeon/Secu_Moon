package com.example.vulnscanner.module.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleTemplateRepository extends JpaRepository<RoleTemplate, Long> {
    Optional<RoleTemplate> findByName(String name);
}
