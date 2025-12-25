package com.example.vulnscanner.module.mocha.repository;

import com.example.vulnscanner.module.mocha.entity.MochaSpdxLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MochaSpdxLicenseRepository extends JpaRepository<MochaSpdxLicense, Long> {
    Optional<MochaSpdxLicense> findByLicenseId(String licenseId);
}