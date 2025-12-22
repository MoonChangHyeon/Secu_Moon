package com.example.vulnscanner.repository;

import com.example.vulnscanner.entity.PackInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PackInfoRepository extends JpaRepository<PackInfo, Long> {
    Optional<PackInfo> findByVersion(String version);

    boolean existsByVersion(String version);
}
