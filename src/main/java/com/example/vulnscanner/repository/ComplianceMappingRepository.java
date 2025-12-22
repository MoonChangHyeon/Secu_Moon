package com.example.vulnscanner.repository;

import com.example.vulnscanner.entity.ComplianceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceMappingRepository extends JpaRepository<ComplianceMapping, Long> {
}
