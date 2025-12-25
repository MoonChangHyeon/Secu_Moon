package com.example.vulnscanner.module.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceMappingRepository extends JpaRepository<ComplianceMapping, Long> {
}