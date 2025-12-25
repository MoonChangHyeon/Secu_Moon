package com.example.vulnscanner.module.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PackInfoRepository extends JpaRepository<PackInfo, Long> {
    Optional<PackInfo> findByVersion(String version);

    boolean existsByVersion(String version);
}