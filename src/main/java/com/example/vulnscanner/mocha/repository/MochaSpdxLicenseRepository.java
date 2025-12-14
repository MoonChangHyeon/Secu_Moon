package com.example.vulnscanner.mocha.repository;

import com.example.vulnscanner.mocha.entity.MochaSpdxLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MochaSpdxLicenseRepository extends JpaRepository<MochaSpdxLicense, Long> {
    Optional<MochaSpdxLicense> findByLicenseId(String licenseId);
}
